package com.ift.http;


import java.util.Map;

public class HttpResponse {
    private Map<String, String> headersMap;
    public final String responseHeader;
    public final String responseBody;
    public final int statusCode;


    static HttpResponse createHttpResponse(int statusCode, String responseHeader, String responseBody) {
        return new HttpResponse(statusCode, responseHeader, responseBody);
    }

    private HttpResponse(int statusCode, String responseHeader, String responseBody) {
        this.responseHeader = responseHeader;
        this.responseBody = responseBody;
        this.statusCode = statusCode;


        if (!responseHeader.contains("HTTP")) {
            throw new IllegalArgumentException("responseHeader is not http packet");
        }


    }

    private void setCookie() {

    }

}
