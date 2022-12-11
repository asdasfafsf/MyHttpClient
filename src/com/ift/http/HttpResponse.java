package com.ift.http;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class HttpResponse {
    private HttpCookie[] httpCookies;
    private Map<String, HttpCookie> httpCookieMap;
    private String[] responseHeaders;
    public final String responseHeader;
    public final String responseBody;
    public final int statusCode;


    protected static HttpResponse createHttpResponse(String responseHeader, String responseBody) {
        return new HttpResponse(responseHeader, responseBody);
    }

    private HttpResponse(String responseHeader, String responseBody) {
        this.responseHeader = responseHeader;
        this.responseBody = responseBody;
        this.responseHeaders = this.responseHeader.split("\r\n");

        if (!responseHeader.contains("HTTP")) {
            throw new IllegalArgumentException("responseHeader is not http packet");
        }

        this.statusCode = Integer.parseInt(responseHeaders[0].split(" ")[1]);
        setHttpCookies();
        setHttpCookieMap();
    }


    public String getHeaderValue(String headerName) {
        return (Arrays.stream(this.responseHeaders)
                .filter(element -> { return element.toLowerCase().startsWith(headerName.toLowerCase()); } )
                .findFirst()
                .orElse(""));
    }


    private void setHttpCookies() {
        this.httpCookies = Arrays.stream(this.responseHeaders)
                .filter(responseHeader -> responseHeader.toLowerCase().startsWith("set-cookie:"))
                .map(setCookie -> {
                    String key = setCookie.split("ookie:")[1].split("=")[0].trim();
                    String value = setCookie.split(key + "=")[1].split(";")[0];
                    String domain = "";

                    HttpCookie httpCookie = HttpCookie.createHttpCookie(key, value, domain);

                    return httpCookie;
                })
                .distinct()
                .toArray(HttpCookie[]::new);
    }

    private void setHttpCookieMap() {
        this.httpCookieMap = new HashMap<String, HttpCookie>();

        for (HttpCookie httpCookie : this.httpCookies) {
            httpCookieMap.put(httpCookie.getKey(), httpCookie);
        }
    }

    public HttpCookie[] getHttpCookies() {
        return this.httpCookies;
    }


    public HttpCookie getHttpCookie(String key) {
        return this.httpCookieMap.get(key);
    }

}
