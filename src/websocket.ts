import {WebSocket} from "ws";
import crawl from "./crawl_task.js";

export default class Websocket {
    connection;

    constructor(url, headers) {
        this.connect(url, headers, );
    }

    connect(url, headers) {
        this.connection = new WebSocket(url, [], {
            headers: headers
        });

        this.connection.on("open", () => {
            console.log("New connection opened");
            this.connection.send("INTEREST");
        });

        this.connection.on("message", async (data) => {
            console.log("Received Crawl Task: " + data);
            if (data) {
                try {
                    for (const task of JSON.parse(data.toString())) {
                        await crawl(task);
                    }

                } catch (e) {
                    console.error("Error receiving Data:\n", e.message);
                }
            }

            console.log("Requesting new Crawl Tasks from Queue");
            this.connection.send("INTEREST");
        });

        this.connection.on("close", () => {
            console.error("Connection closed. Reconnecting...");
            setTimeout(() => {this.connect(url, headers, )}, 5000);
        });

        this.connection.on("error", (e) => {
            console.error("Error on websocket: " + e.message);
        });
    }
}
