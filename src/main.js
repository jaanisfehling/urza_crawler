import {startCrawler} from './base_crawler.js'
import {establishDBConnection} from "./db_adapter.js";

const client = establishDBConnection();
await startCrawler(client);
