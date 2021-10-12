package ru.netology;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Server server = new Server();

        final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        MyHandler fileHandler = (request, response) -> {
            try {
                final Path filePath = Path.of(".", "public", request.getPathWithoutQuery());
                final String mimeType = Files.probeContentType(filePath);

                final long length = Files.size(filePath);
                response.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, response);
                response.flush();
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        };

        MyHandler paramsHandler = (request, response) -> {
            try {
                final Path filePath = Path.of(".", "public", "/classic.html");
                final String mimeType = Files.probeContentType(filePath);
                final String template = Files.readString(filePath);
                final byte[] content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();

                response.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                response.write(content);
                response.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };


        for (String validPath : validPaths) {
            if (validPath.equals("/classic.html")) {
                server.addHandler("GET", validPath, paramsHandler);
            }else {
                server.addHandler("GET", validPath, fileHandler);
            }
        }


        server.listen(9999);
    }
}



