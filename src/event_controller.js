import {saveToDB, urlExistsInDB} from "./db_adapter.js";
import axios from 'axios';
import {log} from "crawlee";
import {Readability} from "@mozilla/readability";
import DOMPurify from "dompurify";

export async function handleArticle(client, absolute_url, headline, datetime, window) {
    if (!await urlExistsInDB(client, absolute_url)) {
        // Scrape article page, strip with mozilla read view and purify against xss attacks
        let readViewHTML = new Readability(window.document).parse().content;
        const purify = DOMPurify(window);
        const sanitizedHTML = purify.sanitize(readViewHTML);

        await saveToDB(client, absolute_url, headline, datetime, sanitizedHTML);
        try {
            const response = await axios.post("http://localhost:8000/new-article/", {
                params: {url: absolute_url}
            });
            log.info("Messaging main application was succesful.");
        } catch (error) {
            log.error("Cannot reach main application.");
        }
    }
}
