import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClientMain {

    public static void main(String[] args) throws Exception{
        HttpResponse httpResponse = HttpRequest
                .createHttpRequest("http://support.infotech.co.kr/support/loginP")
                .setHttpMethod(HttpMethod.GET)
//                .setBody("usrId=asdasd&usrPw=asdas")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .addHeader("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Referer", "http://support.infotech.co.kr/support/login")
                .execute();


//        System.out.println(httpResponse.statusCode);
//        System.out.println(httpResponse.response);

    }
}




enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    OPTION;
}


class HttpRequest {
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
        headersMap.put("Content-length", "");
        headersMap.put("Accept", "application/json, text/plain, */*");
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
        BufferedReader in = null;
        PrintStream out = null;

        StringBuilder responseSb = new StringBuilder();

        try {
            socket = new Socket();
            SocketAddress socketAddress = new InetSocketAddress(this.host, this.port);
            socket.connect(socketAddress, 5000);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());

            String header = this.headersMap.entrySet()
                    .stream()
                    .map(consumer -> { return consumer.getKey() + ": " + consumer.getValue(); })
                    .sorted()
                    .collect(Collectors.joining("\r\n"));


            if (!"".equals(this.body)) {
                String contentType = "";
                contentType = headersMap.get("Content-type");

                if (contentType == null) {
                    contentType = "";
                }
            }

            StringBuilder requestSb = new StringBuilder();

            requestSb.append(this.httpMethod + " " + this.url.toString() + " HTTP/1.1");
            requestSb.append("\r\n");
            requestSb.append("Host: " + this.host);
            requestSb.append("\r\n");
            requestSb.append(header);
            requestSb.append("\r\n");
            requestSb.append("\r\n");

            if (this.httpMethod != HttpMethod.GET) {
                requestSb.append(this.body);
            }

            out.print(requestSb.toString());
            out.flush();

            String line = "";


            while ( ((line = in.readLine()) != null)) {
                responseSb.append(line);
                responseSb.append("\r\n");

                if (line.trim().equals("")) {
                    break;
                }
            }

            String responseHeader = responseSb.toString();
            String[] responseHeaders = responseHeader.split("\r\n");


            int statusCode = Integer.parseInt(responseHeaders[0].split(" ")[1]);

            if (statusCode == 302 && this.followRedirect) {

            }

            String contentLength = (Arrays.stream(responseHeaders)
                    .filter(element -> { return element.toLowerCase().startsWith("content-length"); } )
                    .findFirst()
                    .orElse("Content-Length: " + Integer.MAX_VALUE));

            int responseBodyLength = Integer.parseInt(contentLength.replaceAll("[^\\d]", ""));

            StringBuilder responseBodySb = new StringBuilder();

            while(responseBodySb.length() < responseBodyLength && (line = in.readLine()) != null) {
                responseBodySb.append(line);
                responseBodySb.append("\n");
            }

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
        }


        HttpResponse httpResponse = new HttpResponse(responseSb.toString());
        return httpResponse;
    }
}



class HttpResponse {
    private Map<String, String> headersMap;
    final String response;
    final int statusCode;
    public HttpResponse(String response) {
        this.response = response;
        String[] responses = response.split("\n");

        if (!responses[0].contains("HTTP/1.1")) {
            throw new IllegalArgumentException("response is not http packet");
        }

        int statusCode = Integer.parseInt(responses[0]
                .split("HTTP/1.1")[1]
                .replaceAll("[^\\d]", "")
                .trim());
        this.statusCode = statusCode;

        for (int i = 1; i < responses.length; i++) {
            String res = responses[i];
        }
    }




    public String getHeader(String key) {
        return headersMap.get(key);
    }

}
