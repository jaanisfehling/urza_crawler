import {startBusinessWireCrawler} from './base_crawler.js'
import {establishDBConnection} from "./db_adapter.js";

const client = establishDBConnection();
await startBusinessWireCrawler(client);
