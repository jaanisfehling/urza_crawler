import { RequestQueue, CheerioCrawler } from "crawlee";

let visited = [];

const requestQueue = await RequestQueue.open();
await requestQueue.addRequest({ url: "https://www.businesswire.com/portal/site/home/news/" });

const crawler = new CheerioCrawler({
    requestQueue,
    async requestHandler({ $, request }) {
        $(".bwTitleLink").each(function (i, elem) {

            let headline = $(this).text();
            let article_url = $(this).attr("href");

            if (!visited.includes(headline)) {
                console.log("New Article not visited yet:");
                console.log(headline);
                visited.push(headline);
            }
        });
        // Recrawl the page checking for new articles
        await crawler.addRequests(["https://www.businesswire.com/portal/site/home/news/"]);
    }
})

await crawler.run();
