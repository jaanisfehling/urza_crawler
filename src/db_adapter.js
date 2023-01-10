import * as fs from "fs";
import {Client} from "pg";

export async function establishDBConnection() {
    let settings = JSON.parse(fs.readFileSync("../settings.json", "utf8"));

    const client = new Client({
        host: settings.db.host,
        port: settings.db.port,
        user: settings.db.user,
        password: settings.db.password,
    });
    await client.connect()
    return client;
}

export async function closeDBConnection(client) {
    await client.end()
}

export async function saveToDB(client, url, headline, datetime, html) {
    const text = "INSERT INTO article VALUES($1, $2, $3, $4) RETURNING *"
    const values = [url, headline, datetime, html]

    try {
        return await client.query(text, values);
    } catch (err) {
        return err.stack;
    }
}

export async function URLExistsInDB(client, url) {
    const query = "SELECT URL FROM article WHERE URL='${url}'"
    try {
        const res = await client.query(query);
        if (res.rows[0].equals(url)) {
            return true;
        } else {
            return false;
        }
    } catch {
        return false;
    }
}
