# Use a lightweight Java image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file
COPY target/eureka-server.jar app.jar

# Expose the service port
EXPOSE 8761

# Set the command to run the application
CMD ["java", "-jar", "/app/eureka-server.jar"]