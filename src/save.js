export async function saveToDB(headline, datetime, absolute_url, html) {
    const {Client} = require("pg")
    const client = new Client({
        host: "localhost",
        port: 5432,
        user: "postgres",
        password: "mysecretpassword",
    })
    await client.connect()

    const text = "INSERT INTO article VALUES($1, $2, $3, $4) RETURNING *"
    const values = [headline, datetime, absolute_url, html]
    try {
        const res = await client.query(text, values)
        console.log(res.rows[0])
    } catch (err) {
        console.log(err.stack)
    }

    await client.end()
}