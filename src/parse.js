import {isProbablyReaderable, Readability} from "@mozilla/readability";
import DOMPurify from "dompurify";
import {JSDOM} from "jsdom";
import {parseHTML} from "linkedom";
import { Buffer } from "node:buffer";
import minifyHtml from "@minify-html/node";
import {isMainThread, parentPort, workerData} from "worker_threads";


export function worker(article) {
    const {document} = parseHTML(article.html);

    if (!isProbablyReaderable(document)) {
        return null;
    }

    let reader = new Readability(document).parse();

    article.scraped_date_time = new Date().toISOString();
    article.site_name = (reader.siteName != null) ? reader.siteName.substring(0, 255) : new URL(article.url).hostname;
    article.title = reader.title.substring(0, 255);
    article.excerpt = reader.excerpt;

    const window = new JSDOM().window;
    const purify = DOMPurify(window);
    const purified = purify.sanitize(reader.content);
    article.readable_html = minifyHtml.minify(Buffer.from(purified), {
        keep_spaces_between_attributes: true,
        keep_comments: false
    }).toString();

    return article;
}

if (!isMainThread) {
    parentPort.postMessage(worker(workerData.article));
}