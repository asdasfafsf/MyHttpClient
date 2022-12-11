package com.ift.http;

public class HttpCookie {
    private final String key;
    private final String value;
    private final String domain;


    protected HttpCookie createHttpCookie(String setCookieFullString) {
        return new HttpCookie(setCookieFullString);
    }

    protected HttpCookie(String key, String value, String domain) {
        this.key = key;
        this.value = value;
        this.domain = domain;
    }

    private HttpCookie(String setCookieFullString) {
        this.key = "";
        this.value = "";
        this.domain = "";
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

}
