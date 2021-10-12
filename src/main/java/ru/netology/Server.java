package ru.netology;

import org.apache.http.NameValuePair;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    private final int THREADS = 64;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);

    public void listen(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                threadPool.submit(new ServerThread(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final Map<String, Map<String, MyHandler>> handlers = new HashMap<>();

    public void addHandler(String method, String path, MyHandler handler) {
        Map<String, MyHandler> map = new HashMap<>();
        if (handlers.containsKey(method)) {
            map = handlers.get(method);
        }
        map.put(path, handler);
        handlers.put(method, map);
    }

    private class ServerThread implements Runnable {

        private final Socket socket;
        public static final String GET = "GET";
        public static final String POST = "POST";
        final List allowedMethods = List.of(GET, POST);

        final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try (
                    final BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
                    final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
            ) {

                final var limit = 4096;

                in.mark(limit);
                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
                if (requestLineEnd == -1) {
                    badRequest(out);
                    return;
                }

                final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                if (requestLine.length != 3) {
                    badRequest(out);
                    return;
                }

                final var method = requestLine[0];
                if (!allowedMethods.contains(method)) {
                    badRequest(out);
                    return;
                }
                System.out.println(method);

                final var path = requestLine[1];
                if (!path.startsWith("/")) {
                    badRequest(out);
                    return;
                }
                System.out.println(path);

                final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
                final var headersStart = requestLineEnd + requestLineDelimiter.length;
                final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                if (headersEnd == -1) {
                    badRequest(out);
                    return;
                }

                in.reset();
                in.skip(headersStart);

                final var headersBytes = in.readNBytes(headersEnd - headersStart);
                final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
                System.out.println(headers);
                //System.out.println(requestLine);

                Request request = new Request(method, path, headers);

                if (!method.equals(GET)) {
                    in.skip(headersDelimiter.length);
                    final var contentLength = extractHeader(headers, "Content-Length");
                    if (contentLength.isPresent()) {
                        final var length = Integer.parseInt(contentLength.get());
                        final var bodyBytes = in.readNBytes(length);

                        final var body = new String(bodyBytes);
//                        System.out.println(body);
                    }
                }

                final URI uri = new URI(request.getPath());
                if(uri.getQuery() != null ){
                    //Список GET-параметров
                    List<NameValuePair> nameValuePairs = request.getQueryParams();
                    for (NameValuePair queryParam : nameValuePairs) {
                        System.out.println(queryParam.getName() + " - " + queryParam.getValue());
                    }

                    //Значение GET-параметра по названию одного из параметров
                    request.setQueryParams(nameValuePairs);
                    List<String> valuePairs = request.getQueryParam("param2");
                    System.out.println(valuePairs.get(0));
                }

                MyHandler handler = handlers.get(request.getMethod()).get(request.getPathWithoutQuery());
                if (handler != null) {
                    handler.handle(request, out);
                } else {
                    notFound(out);
                }

            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private static void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }


}