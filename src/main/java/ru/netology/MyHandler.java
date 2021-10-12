package ru.netology;

import java.io.BufferedOutputStream;

@FunctionalInterface
public interface MyHandler {
    void handle(Request request, BufferedOutputStream responseStream);
}