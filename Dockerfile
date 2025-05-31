# Build Java code with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
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

# Install Java (OpenJDK 21) in the runtime image
RUN apt-get update && apt-get install -y openjdk-21-jre-headless

# Run Server
RUN npm install
EXPOSE 3000
CMD ["node", "server.js"] 
