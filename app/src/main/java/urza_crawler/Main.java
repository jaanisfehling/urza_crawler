package urza_crawler;

import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;

public class Main {

    public static void main(String[] args) throws URISyntaxException {
        System.out.println("Running on JVM version " + System.getProperty("java.version"));
        WebSocketClient client = new Client(new URI("ws://localhost:8887"));
        client.connect();
    }
}
