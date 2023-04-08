package urza_crawler;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static URI pipelineUri;
    public static URI queueUri;
    public static WebSocketClient pipelineClient;
    public static WebSocketClient queueClient;
    public static ExecutorService pool = Executors.newFixedThreadPool(4);

    public static void main(String[] args) throws URISyntaxException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));
        System.out.println("Number of Available Processors: " + Runtime.getRuntime().availableProcessors());

        pipelineUri = new URI("ws://localhost:8888");
        pipelineClient = new Client(pipelineUri);
        pipelineClient.connect();

        queueUri = new URI("ws://localhost:8887");
        queueClient = new Client(queueUri);
        queueClient.connect();
    }
}
