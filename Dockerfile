# Build stage
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

# Run stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render usually sets the PORT environment variable
EXPOSE 5000

ENTRYPOINT ["java", "-Dserver.port=${PORT:5000}", "-jar", "app.jar"]
