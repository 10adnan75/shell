# Build Java code with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

# Runtime: Node.js with built Java classes
FROM node:18
WORKDIR /app
COPY package.json .
COPY web ./web
COPY server.js .
COPY --from=build /app/target ./target
RUN npm install
EXPOSE 3000
CMD ["node", "server.js"] 
