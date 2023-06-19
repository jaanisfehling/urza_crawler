// const token = process.env.TOKEN;
import Websocket from "./websocket.js";

const token = "617260221334676695e331cf9742f74924779310";
const headers = {
    "authorization": "Token " + token,
    "origin": "ws://127.0.0.1:8000"
};

export const server = new Websocket("ws://127.0.0.1:8000/news/", headers, false);
export const queue = new Websocket("ws://127.0.0.1:10000", {}, true);
