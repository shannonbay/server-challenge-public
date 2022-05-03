package client;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClientMain {
    private Socket clientSocket;
    private PrintWriter out;
    private InputStream in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        clientSocket.setKeepAlive(true);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = clientSocket.getInputStream();
        logger.info("Connected to " + ip + ":" + port);
    }

    public void sendNonTerminatedMessage(String msg) {
        out.print(msg);
        out.flush();
    }

    public String sendMessage(String msg) throws IOException {
        out.println(msg);
        logger.info("Sent message '" + msg + "'");

        StringBuilder s = new StringBuilder();
        while(true) {

            int i = in.read();
            if(i == -1 || i == '\n'){
                logger.info("Final response: " + s.toString());
                return s.toString();
            } else {
                s.append((char)i);
                logger.info("" + (char)i);
            }
        }
    }

    public void stopConnection() throws IOException {
        if(in != null)
            in.close();
        if(out != null)
            out.close();
        if(clientSocket != null)
            clientSocket.close();
    }

    public static void main(String[] args) throws IOException {
        ClientMain client = new ClientMain();
        client.startConnection("127.0.0.1", 7777);
        logger.info("Got connection to 127.0.0.1:7777");

        String response = client.sendMessage("hello server");
        System.out.println("Done " + response);
        response = client.sendMessage("hello server");
        System.out.println("Done " + response);
        client.stopConnection();
    }

    private static final Logger logger = Logger.getLogger("Client");

}
