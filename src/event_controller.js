import {saveToDB, urlExistsInDB} from "./db_adapter.js";
import axios from 'axios';

export async function handleArticle(client, absolute_url, headline, datetime, html) {
    if (!await urlExistsInDB(client, absolute_url)) {
        // New article
        await saveToDB(client, absolute_url, headline, datetime, html);

        try {
            const response = await axios.get("http://localhost:8000/new-article/", {
                params: {url: absolute_url}
            });
            console.log(response);
        } catch (error) {
            console.error(error);
        }
    }
}
