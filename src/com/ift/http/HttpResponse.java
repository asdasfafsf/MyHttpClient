package com.ift.http;


import java.util.Arrays;
import java.util.Map;

public class HttpResponse {
    private final String[] responseHeaders;
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
    }


    public String getHeaderValue(String headerName) {
        return (Arrays.stream(this.responseHeaders)
                .filter(element -> { return element.toLowerCase().startsWith(headerName.toLowerCase()); } )
                .findFirst()
                .orElse(""));
    }


}
