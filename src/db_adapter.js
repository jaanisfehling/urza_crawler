import * as fs from "fs";
import pg from "pg"
import {log} from "crawlee";

export async function establishDBConnection() {
    let settings = JSON.parse(fs.readFileSync("./settings.json", "utf8"));

    try {
        const client = new pg.Client({
            host: settings.db.host,
            port: settings.db.port,
            user: settings.db.user,
            password: settings.db.password,
        });
        await client.connect();
        return client;
    } catch (e) {
        log.exception(e, "Cannot establish connection.");
    }
}

export async function closeDBConnection(client) {
    try {
        await client.end();
    } catch (e) {
        log.exception(e, "Cannot close connection.");
    }
}

export async function saveToDB(client, url, headline, datetime, html) {
    const text = "INSERT INTO article VALUES($1, $2, $3, $4) RETURNING *"
    const values = [url, headline, datetime, html]

    try {
        return await client.query(text, values);
    } catch (e) {
        log.exception(e, "Cannot save to database.");
    }
}

export async function urlExistsInDB(client, url) {
    const query = "SELECT URL FROM article WHERE URL='${url}'"
    try {
        const res = await client.query(query);
        if (res.rows[0].equals(url)) {
            return true;
        } else {
            return false;
        }
    } catch (e) {
        log.exception(e, "Cannot query database.");
    }
}
