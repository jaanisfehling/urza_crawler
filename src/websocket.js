import {WebSocket} from "ws";
import crawl from "./crawl_task.js";

export default class Websocket {
    connection;

    constructor(url, headers, isQueue) {
        this.connect(url, headers, isQueue);
    }

    connect(url, headers, isQueue) {
        this.connection = new WebSocket(url, [], {
            headers: headers
        });

        this.connection.on("open", () => {
            console.log("New connection opened");
            if (isQueue) {
                this.connection.send("INTEREST");
            }
        });

        this.connection.on("message", async (data) => {
            if (isQueue) {
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
            }
        });

        this.connection.on("close", () => {
            if (isQueue) {
                console.error("Connection closed to Queue. Reconnecting...");
            } else {
                console.error("Connection closed to main server. Reconnecting...");
            }
            setTimeout(() => {this.connect(url, headers, isQueue)}, 5000);
        });

        this.connection.on("error", (e) => {
            if (isQueue) {
                console.error("Error on Queue websocket: " + e.message);
            } else {
                console.error("Error on main server websocket: " + e.message);
            }
        });
    }
}
