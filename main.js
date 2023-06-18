// const token = process.env.TOKEN;
import {connect} from "./websocket";

const token = "2aba21aa2f30c377d51318e02671f57589cd40be";
const headers = {
        "authorization": "Token " + token,
        "origin": "ws://127.0.0.1:8000"
};

export const server = connect("ws://localhost:9000", headers, false);
export const queue = connect("ws://localhost:10000",{}, true);
