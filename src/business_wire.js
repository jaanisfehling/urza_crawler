import {handleArticle} from "./article_handler.js";
import {log} from "crawlee";

export async function businessWire(request, window, dbClient, uniqueKeyCounter) {
    let document = window.document;
    let notVisited = [];

    if (document.querySelector("h1").textContent === "All News") {
        // Scraping news homepage again
        log.info("Business Wire Crawler scraping: " + document.querySelector("h1").textContent.substring(0, 90));

        // Select all Links from listed articles
        document.querySelectorAll(".bwTitleLink").forEach((elem) => {
            let article_url = elem.getAttribute("href");
            notVisited.push({url: "https://www.businesswire.com" + article_url});
        });

        // Visit news homepage again, adding a unique key so we avoid duplicate request mechanism
        await new Promise(r => setTimeout(r, 10000));
        notVisited.push({
            url: "https://www.businesswire.com/portal/site/home/news/",
            uniqueKey: uniqueKeyCounter.toString()
        });
    } else {
        const headline = document.querySelector("h1").textContent;
        log.info("Business Wire Crawler scraping: " + headline.substring(0, 90));
        const datetime = document.querySelector("time").getAttribute("datetime").replace('T', ' ').replace('Z', '') + "-05";

        // Save to database
        await handleArticle(dbClient, headline, datetime, request.url, window);
    }
    return notVisited;
}
