import {RequestQueue, JSDOMCrawler} from "crawlee";
import {business_wire} from "./business_wire.js";

export async function startCrawler(client, businessWire = true, globeNewswire = false) {
    // We need to create a uniqueKey when scraping an already visited URL, so we use a counter
    let uniqueKeyCounter = 0;

    const crawler = new JSDOMCrawler({
        async requestHandler({request, window}) {

            uniqueKeyCounter++;
            const notVisited = await business_wire(request, window, client, uniqueKeyCounter);
            await crawler.addRequests(notVisited);

        }
    });

    let startURLs = [];
    if (businessWire) {
        startURLs.push(["https://www.businesswire.com/portal/site/home/news/"]);
    }
    if (globeNewswire) {
        startURLs.push(["https://www.globenewswire.com/search/?pageSize=50"]);
    }
    await crawler.run(startURLs);
}
