package ru.netology;

public class RequestLine {

    private final String method;
    private final String path;

    public RequestLine(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

}
