import {Readability} from "@mozilla/readability";
import DOMPurify from "dompurify";
import {handleArticle} from "./event_controller.js";

export async function business_wire(request, window, client, uniqueKeyCounter) {
    let document = window.document;
    let notVisited = [];

    if (document.querySelector("h1").textContent === "All News") {
        // Scraping news homepage again
        console.log("Scraping: " + document.querySelector("h1").textContent.substring(0, 90));

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
        console.log("Scraping: " + headline.substring(0, 90));

        const datetime = document.querySelector("time").getAttribute("datetime").replace('T', ' ').replace('Z', '') + "-05";

        // Scrape article page, strip with mozilla read view and purify against xss attacks
        let readViewHTML = new Readability(document).parse().content;
        const purify = DOMPurify(window);
        const sanitizedHTML = purify.sanitize(readViewHTML);

        // Save to database
        await handleArticle(client, headline, datetime, request.url, sanitizedHTML);
    }
    return notVisited;
}