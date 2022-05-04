package server;

import java.net.*;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ServerMain {
    private ServerSocket serverSocket;

    private ConcurrentLinkedDeque<Connection> connections = new ConcurrentLinkedDeque<>();

    private final ExecutorService executor = Executors.newFixedThreadPool(4); // Q1: Explain why 1 thread won't work

    public void start(final int port) throws IOException, InterruptedException {

        try {
            serverSocket = new ServerSocket(port);
        } catch (BindException e) {
            Thread.sleep(500); // wait half a second and retry once
            serverSocket = new ServerSocket(port);
        }
        executor.execute(() -> {
            try {
                while (true) {
                    Connection c = new Connection(serverSocket.accept(), logger);
                    connections.offer(c);
               }
            } catch (IOException e) {
                logger.severe("No longer accepting connections");
            }
        });

        executor.execute(new ProcessConnections());

        logger.info("Finished booting server");
    }

    public void stop() throws IOException {
        for(Connection c: connections) {
            c.stop();
        }
        executor.shutdown();

        connections.clear();
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("location: " + System.getProperty("java.util.logging.config.file"));
        ServerMain server=new ServerMain();
        server.start(7777);
    }

    private static final Logger logger = Logger.getLogger("Server");

    private class ProcessConnections implements Runnable {
        @Override
        public void run() {
            while(true) {
                for(final Connection c: connections) {
                    if(c.state.compareAndSet(Connection.ConnState.waiting, Connection.ConnState.running)) {
                        logger.finest("Processing connection " + c);
                        // TODO #4 Run next two lines asynchronously to process multiple clients simultaneously
                        c.run();
                        c.state.compareAndSet(Connection.ConnState.running, Connection.ConnState.waiting);
                        logger.finest("Finished processing connection " + c);
                    }
                }
            }
        }
    }
}
