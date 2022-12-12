package com.ift.http;

public class HttpCookie {
    private final String key;
    private final String value;
    private final String domain;


    protected static HttpCookie createHttpCookie(String key, String value, String domain) {
        return new HttpCookie(key, value, domain);
    }

    protected HttpCookie(String key, String value, String domain) {
        this.key = key;
        this.value = value;
        this.domain = domain;
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public String getDomain() {
        return this.domain;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof HttpCookie) {
            HttpCookie target = (HttpCookie) o;

            return this.key.equals(target.key) && this.domain.equals(target.domain);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode() + this.domain.hashCode();
    }


    @Override
    public String toString() {
        return this.key + "=" + this.value + ";" + this.domain;
    }

}
