FROM node:18-slim as compile-image

ENV NODE_ENV=development

COPY . .

RUN npm ci
RUN npm install -g typescript@5.2.2

RUN tsc --skipLibCheck


FROM node:18-slim

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=compile-image dist/main.js dist/crawl_task.js dist/websocket.js ./
COPY src/parse.js src/parse.js

CMD node main.js