package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private String method;
    private String path;
    private List<String> headers;
    private String body;
    private List<NameValuePair> queryParams;

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }
    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }
    public String getPath() {
        return path;
    }
    public List<String> getHeaders() { return headers; }
    public String getBody() { return body; }

    public String getPathWithoutQuery() throws URISyntaxException {
        final URI returnPath = new URI(path);
        return returnPath.getPath();
    }

    public List<NameValuePair> getQueryParams() throws URISyntaxException {
        final URI uri = new URI(path);
        List<NameValuePair> nameValuePairs = URLEncodedUtils.parse(uri, Charset.forName("utf-8"));
        return nameValuePairs;
    }

    public List<String> getQueryParam(String name) {
                return queryParams.stream()
                .filter(o -> o.getName().startsWith(name))
                .map(NameValuePair::getValue)
                .collect(Collectors.toList());
    }

}