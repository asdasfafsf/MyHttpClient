package com.ift.http;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {
    private URL url;
    private String host;
    private int port;
    private String protocol;
    private HttpMethod httpMethod;
    private Map<String, String> headersMap;
    private Proxy proxy;
    private boolean followRedirect = true;

    private String body = "";

    private HttpRequest(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("malformed url");
        }

        this.port = this.url.getPort() != -1 ? this.url.getPort() : this.url.getDefaultPort();
        this.protocol = this.url.getProtocol();
        this.host = this.url.getHost();

        headersMap = new HashMap<>();
        headersMap.put("Connection", "keep-alive");
        headersMap.put("Accept", "*/*");
        headersMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
        headersMap.put("Origin", this.protocol + "://" + this.host);

        this.httpMethod = HttpMethod.GET;
    }


    public static HttpRequest createHttpRequest(String url) {
        return new HttpRequest(url);
    }


    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;

        return this;
    }


    public HttpRequest setFollowRedirect(boolean followRedirect) {
        this.followRedirect = followRedirect;

        return this;
    }


    public HttpRequest addHeader(String key, String value) {
        if (this.headersMap.get(key) != null) {
            this.headersMap.remove(key);
        }

        this.headersMap.put(key, value);

        return this;
    }


    public HttpRequest setBody(String body) {
        this.body = body;

        this.addHeader("Content-length", String.valueOf(this.body.length()));

        return this;
    }

    public HttpRequest setProxy(Proxy proxy) {
        this.proxy = proxy;

        return this;
    }


    public HttpRequest setProxy(String source, int port) {
        SocketAddress proxyAddress = new InetSocketAddress(source, port);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);

        return this.setProxy(proxy);
    }


    public HttpResponse execute() throws Exception {
        Socket socket = null;
        SSLSocket sslSocket = null;
        BufferedReader in = null;
        PrintStream out = null;

        StringBuilder responseHeaderSB = new StringBuilder();
        StringBuilder responseBodySB = new StringBuilder();
        int statusCode = 0;


        try {


            if ("HTTPS".equalsIgnoreCase(this.protocol)) {
                SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                sslSocket = (SSLSocket) sslSocketFactory.createSocket(this.host, this.port);
                out = new PrintStream(sslSocket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            } else {
                SocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
                socket = new Socket();
                socket.connect(socketAddress, 5000);
                out = new PrintStream(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }


            String requestHeader = this.getRequestHeader();
            StringBuilder requestSb = new StringBuilder();

            requestSb.append(this.httpMethod + " " + this.url.toString() + " HTTP/1.1");
            requestSb.append("\r\n");
            requestSb.append("Host: " + this.host);
            requestSb.append("\r\n");
            requestSb.append(requestHeader);
            requestSb.append("\r\n");
            requestSb.append("\r\n");

            if (this.httpMethod != HttpMethod.GET) {
                requestSb.append(this.body);
            }
            System.out.println(requestSb.toString());
            out.print(requestSb.toString());
            out.flush();

            String line = "";

            while ( ((line = in.readLine()) != null)) {
                responseHeaderSB.append(line);
                responseHeaderSB.append("\r\n");

                if (line.trim().equals("")) {
                    break;
                }
            }

            String[] responseHeaders = responseHeaderSB.toString().split("\r\n");


            String contentLength = (Arrays.stream(responseHeaders)
                    .filter(element -> { return element.toLowerCase().startsWith("content-length"); } )
                    .findFirst()
                    .orElse("Content-Length: " + Integer.MAX_VALUE));

            int responseBodyLength = Integer.parseInt(contentLength.replaceAll("[^\\d]", ""));

            System.out.println(responseBodyLength);

            while(responseBodySB.length() < responseBodyLength && (line = in.readLine()) != null && !"00000000".equals(line)) {
                responseBodySB.append(line);
                responseBodySB.append("\r\n");
            }

            System.out.println(responseBodySB.length());

            statusCode = Integer.parseInt(responseHeaders[0].split(" ")[1]);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }

            if (in != null) {
                in.close();
            }

            if (socket != null) {
                socket.close();
            }

            if (sslSocket != null) {
                sslSocket.close();
            }
        }


        if (statusCode == 302 && this.followRedirect) {
            String location = (Arrays.stream(responseHeaderSB.toString().split("\r\n"))
                    .filter(element -> { return element.toLowerCase().startsWith("location"); } )
                    .findFirst()
                    .orElseThrow(() -> new Exception("Can not found redirect location") ));
            String redirectUrl = this.protocol + "://" + this.host + location.split(":")[1].trim();

            System.out.println(location);
            System.out.println(redirectUrl + "야기로 이동하자!!");

            return createHttpRequest(redirectUrl)
                    .addHeader("Referer", this.url.toString())
                    .execute();
        }


        HttpResponse httpResponse =  HttpResponse.createHttpResponse(statusCode, responseHeaderSB.toString(), responseBodySB.toString());

        return httpResponse;
    }


    private String getRequestHeader() {
        return this.headersMap.entrySet()
                .stream()
                .map(consumer -> { return consumer.getKey() + ": " + consumer.getValue(); })
                .sorted()
                .collect(Collectors.joining("\r\n"));
    }



}