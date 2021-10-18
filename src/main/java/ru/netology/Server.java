package ru.netology;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        private final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
        private final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};

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

                RequestLine requestLine = getRequestLine(buffer, read);
                if (requestLine == null) {
                    badRequest(out);
                    return;
                }

                List<String> headers = getHeaders(buffer, read, in);
                if (headers == null) {
                    badRequest(out);
                    return;
                }

                Request request = new Request(requestLine.getMethod(), requestLine.getPath(), headers, getBody(headers, in));

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


        private RequestLine getRequestLine(byte[] buffer, int read) {
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                return null;
            }

            final var rLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (rLine.length != 3) {
                return null;
            }

            if (!allowedMethods.contains(rLine[0])) {
                return null;
            }

            if (!rLine[1].startsWith("/")) {
                return null;
            }

            return new RequestLine(rLine[0], rLine[1]);
        }

        private List<String> getHeaders(byte[] buffer, int read, BufferedInputStream in) throws IOException {
            final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

            final int headersStart = requestLineEnd + requestLineDelimiter.length;
            final int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                return null;
            }

            in.reset();
            in.skip(headersStart);

            final byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
            return Arrays.asList(new String(headersBytes).split("\r\n"));
        }

        private String getBody(List<String> headers, BufferedInputStream in) throws IOException {
            in.skip(headersDelimiter.length);
            final var contentLength = extractHeader(headers, "Content-Length");
            if (contentLength.isPresent()) {
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);
                final var body = new String(bodyBytes);
                return body;
            }else{
                return "";
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