FROM node:18.16.1

RUN apt-get update && apt-get -q -y install openjdk-17-jdk curl
RUN curl -s https://download.clojure.org/install/linux-install-1.10.3.1087.sh | bash

WORKDIR /tmp/open-llm

COPY package.json package-lock.json ./
RUN npm install
RUN npm ci && npm cache clean --force
COPY . ./
RUN npm run release
