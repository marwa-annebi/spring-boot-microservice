# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/like-service.jar like-service.jar

# Expose the Like service port
EXPOSE 8088

# Run the jar file
ENTRYPOINT ["java", "-jar", "like-service.jar"]