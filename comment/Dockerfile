# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/comment-service.jar comment-service.jar

# Expose the Comment service port
EXPOSE 8089

# Run the jar file
ENTRYPOINT ["java", "-jar", "comment-service.jar"]