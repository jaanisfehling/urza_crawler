import {startCrawler} from './base_crawler.js'
import fs from "fs";
import pg from "pg";

let settings = JSON.parse(fs.readFileSync("./settings.json", "utf8"));
const client = new pg.Client({
    host: settings.db.host,
    port: settings.db.port,
    user: settings.db.user,
    password: settings.db.password,
});
await client.connect();

await startCrawler(client);

await client.end();
