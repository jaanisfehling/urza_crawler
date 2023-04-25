package urza_crawler;

import org.java_websocket.client.WebSocketClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    public static Logger logger = Logger.getLogger("Urza Crawler");
    public static URI pipelineUri;
    public static URI queueUri;
    public static WebSocketClient pipelineClient;
    public static WebSocketClient queueClient;
    public static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws URISyntaxException {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (SecurityException | IOException e) {
            throw new RuntimeException(e);
        }

        logger.log(Level.CONFIG, "Running on JVM version " + System.getProperty("java.version"));
        logger.log(Level.CONFIG, "Number of Available Processors: " + Runtime.getRuntime().availableProcessors());

        pipelineUri = new URI("ws://localhost:9000");
        pipelineClient = new Client(pipelineUri);
        pipelineClient.connect();

        queueUri = new URI("ws://localhost:10000");
        queueClient = new Client(queueUri);
        queueClient.connect();
    }
}
