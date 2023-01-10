import {startBusinessWireCrawler} from './businessWire.js'
import {establishDBConnection} from "./db_adapter.js";

const client = establishDBConnection();
await startBusinessWireCrawler(client);
