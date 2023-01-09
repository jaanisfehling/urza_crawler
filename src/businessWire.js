import {RequestQueue, JSDOMCrawler} from "crawlee";
import {Readability} from "@mozilla/readability";
import DOMPurify from 'dompurify';
import {saveToDB} from "./save.js";

let counter = 0;

const requestQueue = await RequestQueue.open();
await requestQueue.addRequest({url: "https://www.businesswire.com/portal/site/home/news/"});

export const businessWireCrawler = new JSDOMCrawler({
    requestQueue,
    async requestHandler({request, window}) {
        let document = window.document;
        let not_visited = [];

        if (document.querySelector("h1").textContent == "All News") {
            // Scraping news homepage again
            console.log("Scraping: " + document.querySelector("h1").textContent.substring(0, 90));

            // Select all Links from listed articles
            document.querySelectorAll(".bwTitleLink").forEach((elem) => {
                let article_url = elem.getAttribute("href");
                not_visited.push({url: "https://www.businesswire.com" + article_url});
            });

            // Visit news homepage again, adding a unique key so we avoid duplicate request mechanism
            await new Promise(r => setTimeout(r, 10000));
            counter++;
            not_visited.push({
                url: "https://www.businesswire.com/portal/site/home/news/",
                uniqueKey: counter.toString()
            });
        } else {
            let headline = document.querySelector("h1").textContent;
            console.log("Scraping: " + headline.substring(0, 90));

            let datetime = document.querySelector("time").getAttribute("datetime").replace('T', ' ').replace('Z', '') + "-05";

            // Scrape article page, strip with mozilla read view and purify against xss attacks
            let article = new Readability(document).parse().content;
            const purify = DOMPurify(window);
            const cleaned_article = purify.sanitize(article);

            // Save to database
            await saveToDB(headline, datetime, request.url, cleaned_article);
        }

        await businessWireCrawler.addRequests(not_visited);

    }
});
