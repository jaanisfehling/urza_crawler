import {WebSocket} from "ws";
import CrawlTask from "./crawl_task.js";

export default class Websocket {
    ws;

    constructor(url, headers, isQueue) {
        connect(url, headers, isQueue);
    }
}
export function connect(url, headers, isQueue) {
    this.ws = new WebSocket(url, [], {
        headers: headers
    });

    this.ws.on("open", function() {
        console.log("New connection opened");
        if (isQueue) {
            this.ws.send("INTEREST");
        }
    });

    this.ws.on("message", function message(data) {
        console.log("Received Crawl Task: " + message);

        if (data && data !== "") {
            try {
                for (const crawlTask of JSON.parse(data.toString())) {
                    new CrawlTask(...crawlTask);
                }

            } catch (e) {
                console.error("Error receiving Data:\n", e.message);
            }
        }

        console.log("Requesting new Crawl Tasks from Queue");
        this.ws.send("INTEREST");
    });

    this.ws.on("close", function() {
        console.error("Connection closed to main server. Reconnecting...");
        setTimeout(connect, 5000, url, headers, isQueue);
    });

    this.ws.on("error", function() {
        console.error("Error on main server websocket");
    });
}
