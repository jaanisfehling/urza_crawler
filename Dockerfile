FROM node:18-alpine3.17

WORKDIR /app

COPY package*.json ./

RUN npm install

RUN npm ci --omit=dev

COPY . .

EXPOSE 9000

CMD node main.js