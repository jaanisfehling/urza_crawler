import {JSDOMCrawler} from "crawlee";
import {businessWire} from "./business_wire.js";

export async function startCrawler(client, business_wire = true, globe_newswire = false) {
    // We need to create a uniqueKey when scraping an already visited URL, so we use a counter
    let uniqueKeyCounter = 0;

    const crawler = new JSDOMCrawler({
        async requestHandler({request, window}) {
            uniqueKeyCounter++;
            let notVisited = [];
            if (business_wire) {
                notVisited = notVisited.concat(await businessWire(request, window, client, uniqueKeyCounter));
            }
            await crawler.addRequests(notVisited);
        }
    });

    let startURLs = [];
    if (business_wire) {
        startURLs.push("https://www.businesswire.com/portal/site/home/news/");
    }
    if (globe_newswire) {
        startURLs.push("https://www.globenewswire.com/search/?pageSize=50");
    }
    await crawler.run(startURLs);
}
