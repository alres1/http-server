package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int port;
    private final int threads;

    public Server() {
        this.port = 9999;
        this.threads = 64;
    }


    @Override
    public void run() {

        ExecutorService threadPool = Executors.newFixedThreadPool(threads);

        try{
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(socket);

                ServerThread st = new ServerThread(socket);
                threadPool.execute(st);
            }
        }catch(IOException e){
            System.out.println(e);
        }

        threadPool.shutdown();
    }
}
