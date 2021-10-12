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
    private List<NameValuePair> queryParams;

    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }
    public String getPath() {
        return path;
    }
    public String getPathWithoutQuery() throws URISyntaxException {
        final URI returnPath = new URI(path);
        return returnPath.getPath();
    }

    public void setQueryParams(List<NameValuePair> params) {
        this.queryParams = params;
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