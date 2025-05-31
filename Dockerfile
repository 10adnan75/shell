FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

FROM node:18
WORKDIR /app
COPY package.json .
COPY web ./web
COPY server.js .
COPY --from=build /app/target ./target
RUN npm install
EXPOSE 3000
CMD ["node", "server.js"]
