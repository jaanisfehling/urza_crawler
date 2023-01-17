import {startCrawler} from './base_crawler.js'
import {closeDBConnection, establishDBConnection} from "./db_adapter.js";

const client = establishDBConnection();
await startCrawler(client);
await closeDBConnection(client);
