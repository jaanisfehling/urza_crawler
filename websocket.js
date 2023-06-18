import {WebSocket} from "ws";
import CrawlTask from "./crawl_task";

export function connect(url, headers, isQueue) {
    const ws = new WebSocket(url, [], {
        headers: headers
    });

    ws.on("open", function() {
        console.log("New connection opened");
        if (isQueue) {
            ws.send("INTEREST");
        }
    });

    ws.on("message", function message(data) {
        console.log("Received Crawl Task: " + message);

        if (data && data !== "") {
            try {
                for (const crawlTask of JSON.parse(data.toString())) {
                    let crawlTaskObj = new CrawlTask(...crawlTask);
                }

            } catch (e) {
                console.error("Error receiving Data:\n", e.message);
            }
        }

        console.log("Requesting new Crawl Tasks from Queue");
        ws.send("INTEREST");
    });

    ws.on("close", function() {
        console.error("Connection closed to main server. Reconnecting...");
        setTimeout(connect, 5000, [url, headers, isQueue]);
    });

    ws.on("error", function() {
        console.error("Error on main server websocket");
    });

    return ws;
}
