# Build Stage
FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /app
COPY back/rhythm_game/pom.xml .
RUN mvn dependency:go-offline
COPY back/rhythm_game/src ./src
RUN mvn package -DskipTests

# Run Stage
FROM openjdk:17-slim
WORKDIR /app

# Install system dependencies: ffmpeg and webp
RUN apt-get update && apt-get install -y \
    ffmpeg \
    webp \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

# Application Configuration
ENV SERVER_PORT=8090
EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]
