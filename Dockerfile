# ====== Stage 1: Build the application ======
FROM maven:3.9.6-eclipse-temurin-23 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven descriptor files first (for dependency caching)
COPY pom.xml .
COPY src ./src

# Package the application (skip tests to speed up build)
RUN mvn clean package -DskipTests

# ====== Stage 2: Run the application ======
FROM openjdk:23-slim

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/studentapi-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (Render automatically maps it)
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
