import client.ClientMain;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import server.ServerMain;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class WhenUsingServerMain {

    ServerMain server=new ServerMain();
    ClientMain client, client2, client3;

    @BeforeClass
    public static void setupClass(){
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }

    @Before
    public void setup() throws IOException, InterruptedException {
        System.out.println("Creating server listen on port 7777");
        server.start(7777);

        client = new ClientMain();
        client2 = new ClientMain();
        client3 = new ClientMain();
    }

    @Test
    public void successiveClientRequest() throws IOException {

        System.out.println("Creating client");
        client.startConnection("127.0.0.1", 7777);
        System.out.println("Connected to server");
        String response = client.sendMessage("hello server");
        System.out.println("Sent message");
        assertEquals("hello client", response);

        response = client.sendMessage("hello server");
        System.out.println("Sent message");
        assertEquals("hello client", response);

    }

    @Test
    public void multipleClients() throws IOException {

        System.out.println("Creating client 1");
        client.startConnection("127.0.0.1", 7777);
        System.out.println("Connected to server");

        System.out.println("Creating client 2");
        client2.startConnection("127.0.0.1", 7777);
        System.out.println("Connected to server");

        client.sendNonTerminatedMessage("hello ");
        System.out.println("Sent non-terminated message on client 1");

        String response = client2.sendMessage("hello server");
        System.out.println("Sent message on client 2");
        assertEquals("hello client", response);

        response = client.sendMessage("server");
        System.out.println("Sent message on client 1");
        assertEquals("hello client", response);

    }

    @Test // TODO #1: add the joke protocol to Connection.java
    public void knockknock() throws IOException {
        System.out.println("Creating client");
        client.startConnection("127.0.0.1", 7777);
        System.out.println("Connected to server");
        String response = client.sendMessage("joke");
        System.out.println("Asked server for a joke");
        assertEquals("knock knock", response);

        response = client.sendMessage("who's there?");
        System.out.println("Asked \"who's there?\"");
        assertEquals("maya", response);

        response = client.sendMessage("maya who?");
        System.out.println("Asked \"maya who?\"");
        assertEquals("maya ho, maya haha!", response);
    }

    @Test // TODO #2: write this test and handle the failure somehow
    public void clientRespondsToKnockKnockIncorrectly(){
        fail();
    }

    @Test // TODO #3: write this test and handle the failure somehow
    public void clientTimesOutAfterKnockKnock(){
        fail();
    }

    /**
     * TODO #4: two clients send simultaneous count commands, but server processes them synchronously
     * leading to a timeout
     * Update lines 67-68 of ServerMain.java
    */
    @Test
    public void count() throws IOException, InterruptedException, TimeoutException, ExecutionException {
        logger.info("Creating client");
        client.startConnection("127.0.0.1", 7777);
        client2.startConnection("127.0.0.1", 7777);
        System.out.println("Connected to server");

        // submit two 6 second count commands simultaneously
        ExecutorService es = Executors.newFixedThreadPool(2);
        Future<String> f = es.submit(() -> {
            try {
                return client.sendMessage("count");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
        Future<String> f2 = es.submit(() -> {
            try {
                return client2.sendMessage("count");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });

        String response = f.get(7, TimeUnit.SECONDS);
        assertEquals("5 4 3 2 1 0 ", response);
        logger.info("First count complete");
        response = f2.get(1, TimeUnit.SECONDS);
        assertEquals("5 4 3 2 1 0 ", response);
        logger.info("Second count complete");
        Thread.sleep(2000);

        response = client.sendMessage("hello server");
        System.out.println("Sent message on client 2");
        assertEquals("hello client", response);
    }

    @After
    public void stop() throws IOException {
        System.out.println("Shutting down server");
        server.stop();

        client.stopConnection();
        client2.stopConnection();
        client3.stopConnection();
    }

    Logger logger = Logger.getLogger("test");
}
