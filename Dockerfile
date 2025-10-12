# --- Stage 1: Build the application ---
# Use a Maven image to build the project
FROM maven:3.9-eclipse-temurin-23-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the pom.xml file first to leverage Docker's layer caching
COPY pom.xml .

# Copy the rest of the source code
COPY src ./src

# Run Maven to build the application and create the .jar file
# The -DskipTests flag speeds up the build by skipping tests
RUN mvn package -DskipTests


# --- Stage 2: Create the final, lightweight image ---
# Use a slim Java runtime image because we only need to run the app now
FROM openjdk:23-slim

# Set the working directory
WORKDIR /app

# Copy the .jar file that was created in the 'build' stage
COPY --from=build /app/target/studentapi-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# The command to run when the container starts
CMD ["java", "-jar", "app.jar"]