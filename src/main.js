import {startCrawler} from './base_crawler.js'
import fs from "fs";
import pg from "pg";
import {log} from "crawlee";

for (;;) {
    try {
        let settings = JSON.parse(fs.readFileSync("./settings.json", "utf8"));
        const client = new pg.Client({
            host: settings.db.host,
            port: settings.db.port,
            user: settings.db.user,
            password: settings.db.password,
        });
        await client.connect();
        break;
    } catch (e) {
        log.exception(e, "Cannot connect to database.");
        log.info("Retrying in 5 Seconds");
        await new Promise(r => setTimeout(r, 10000));
        log.info("Retrying...");
    }
}

await startCrawler(client);

await client.end();
