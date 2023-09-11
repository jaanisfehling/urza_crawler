FROM node:18-alpine3.17 as compile-image

RUN npm i typescript

COPY src/ .

RUN tsc main.ts
RUN tsc parse.ts


FROM node:18-alpine3.17

COPY package*.json ./
RUN npm ci --omit=dev

COPY --from=compile-image *.js .

CMD node main.js