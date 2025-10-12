# Use an official lightweight Java 23 image
FROM openjdk:23-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the packaged jar file from your 'target' folder into the container
COPY target/studentapi-0.0.1-SNAPSHOT.jar app.jar

# Tell Docker that the container will listen on port 8080
EXPOSE 8080

# The command to run when the container starts
CMD ["java", "-jar", "app.jar"]