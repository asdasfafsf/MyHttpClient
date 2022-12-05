import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpClientMain {

    public static void main(String[] args) throws Exception{
        HttpResponse httpResponse = HttpRequest
                .createHttpRequest()
                .setUrl("http://leesungdang1945.com/")
                .setHttpMethod(HttpMethod.GET)
                .addHeader("User-Agent", "")
                .execute();
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

    private Byte[] postDataBytes;

    private Map<String, String> headersMap;
    private Proxy proxy;



    private HttpRequest() {
        headersMap = new HashMap<>();
        headersMap.put("Accept", "application/json, text/plain, */*");
        headersMap.put("User-Agent", "");


        this.httpMethod = HttpMethod.GET;
    }


    public static HttpRequest createHttpRequest() {
        return new HttpRequest();
    }


    public HttpRequest setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("malformed url");
        }

        this.port = this.url.getPort() != -1 ? this.url.getPort() : this.url.getDefaultPort();
        this.protocol = this.url.getProtocol();
        this.host = this.url.getHost();

        return this;
    }

    public HttpRequest setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;

        return this;
    }


    public HttpRequest setProxy(Proxy proxy) {
        this.proxy = proxy;

        return this;
    }

    public HttpRequest addHeader(String key, String value) {
        if (this.headersMap.get(key) != null) {
            this.headersMap.remove(key);
        }

        this.headersMap.put(key, value);

        return this;
    }


    public HttpRequest setProxy(String source, int port) {
        SocketAddress proxyAddress = new InetSocketAddress(source, port);
        Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxyAddress);

        return this.setProxy(proxy);
    }


    public HttpRequest setBody(Byte[] postDataBytes) {
        this.postDataBytes = postDataBytes;
        headersMap.put("Content-length", String.valueOf(this.postDataBytes.length));

        return this;
    }


    public HttpResponse execute() throws Exception {
        if (this.url == null) {
            throw new Exception("url is not null");
        }

        Socket socket = null;
        BufferedReader in = null;
        PrintStream out = null;


        StringBuilder sb = new StringBuilder();

        try {
            socket = new Socket(this.host, this.port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintStream(socket.getOutputStream());

            out.println(this.httpMethod + " " + this.url.toString() + " " + this.protocol.toUpperCase() + "/1.1");
            out.println("Host: " + this.host);

            String header = this.headersMap.entrySet()
                    .stream()
                    .map(consumer -> { return consumer.getKey() + ": " + consumer.getValue() + "\n"; })
                    .collect(Collectors.joining());

            out.print(header);
            out.println();

            String line = null;

            while ((line = in.readLine()) != null) {
                sb.append(line);
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

        return new HttpResponse(sb.toString());
    }
}



class HttpResponse {
    private Map<String, String> headersMap;
    private String response;
    private int statusCode;
    public HttpResponse(String response) {

    }
}
