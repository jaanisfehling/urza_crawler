export async function saveToDB(headline, absolute_url, html) {
    const { Client } = require("pg")
    const client = new Client({
        host: "localhost",
        port: 5432,
        user: "postgres",
        password: "mysecretpassword",
      })
    await client.connect()
    
    await client.end()
}
