import {log} from "crawlee";

export async function saveToDB(client, url, headline, datetime, html) {
    const text = "INSERT INTO article VALUES($1, $2, $3, $4) RETURNING *"
    const values = [url, headline, datetime, html]

    try {
        const res = await client.query(text, values);
        return res;
    } catch (e) {
        log.exception(e, "Cannot save to database.");
    }
}

export async function urlExistsInDB(client, url) {
    const query = "SELECT URL FROM article WHERE URL=$1"
    try {
        const res = await client.query(query, [url]);
        if (res.rowCount === 0) {
            return false;
        } else {
            return true;
        }
    } catch (e) {
        log.exception(e, "Cannot query database.");
    }
}
