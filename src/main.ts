import Websocket from "./websocket.js";


export const newArticleEndpoint = "http://localhost:8000/api/news/trigger-pipeline/";
export const scraperKey = "xxxxxxxx";
export const apiToken = "asdfsafdhajgsdfgkasdfghkjasgfdkjhas";

export const queue = new Websocket("ws://127.0.0.1:9000", {});
