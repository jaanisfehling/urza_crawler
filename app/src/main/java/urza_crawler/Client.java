package urza_crawler;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class Client extends WebSocketClient {

    public Client(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("New connection opened");
        send("");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " Additional info: " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("Received message: " + message);

        Gson gson = new Gson();
        List<Callable<Boolean>> crawlTasks = Arrays.asList(gson.fromJson(message, CrawlTask[].class));

        try {
            List<Future<Boolean>> futures = Main.pool.invokeAll(crawlTasks);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        send("");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred:" + ex);
    }
}
