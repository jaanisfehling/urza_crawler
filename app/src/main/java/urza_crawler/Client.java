package urza_crawler;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import static urza_crawler.Main.queueUri;

public class Client extends WebSocketClient {
    ScheduledExecutorService executorService;

    public Client(URI serverURI) {
        super(serverURI);
        executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("New connection opened");
        if (uri.equals(queueUri)) {
            send("");
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with exit code " + code + " Additional info: " + reason);
        executorService.schedule(new Reconnect(this), 5, TimeUnit.SECONDS);
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
