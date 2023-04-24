package urza_crawler;

import org.java_websocket.client.WebSocketClient;

public class Reconnect implements Runnable {
    WebSocketClient client;

    public Reconnect(WebSocketClient client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.reconnect();
    }
}
