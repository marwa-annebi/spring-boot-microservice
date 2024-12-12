# Use OpenJDK 17 base image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/api-gateway.jar api-gateway.jar

# Expose the API Gateway port
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]