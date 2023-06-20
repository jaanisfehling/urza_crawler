// const token = process.env.TOKEN;
import Websocket from "./websocket.js";

const token = "e47e5a015230f12b3dceba38178f4441c551d90e";
const headers = {
    "authorization": "Token " + token,
    "origin": "ws://127.0.0.1:8000"
};

export const server = new Websocket("ws://127.0.0.1:8000/news/", headers, false);
export const queue = new Websocket("ws://127.0.0.1:10000", {}, true);
