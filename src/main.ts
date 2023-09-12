import Websocket from "./websocket.js";


export const newArticleEndpoint = "http://host.docker.internal:8000/api/news/trigger-pipeline/";
export const scraperKey = "aaa-bbb-ccc";
export const apiToken = "f0f903acd85f2b07bbf7310bc5cbb1d2f7815c1b";

export const queue = new Websocket("ws://host.docker.internal:9000", {});
