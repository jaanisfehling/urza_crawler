import {saveToDB, urlExistsInDB} from "./db_adapter.js";

export async function handleArticle(client, absolute_url, headline, datetime, html) {
    if (!await urlExistsInDB(client, absolute_url)) {
        await saveToDB(client, absolute_url, headline, datetime, html);
    }
}
