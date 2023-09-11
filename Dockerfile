FROM node:18-alpine3.17

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

COPY src/ .

CMD node main.js