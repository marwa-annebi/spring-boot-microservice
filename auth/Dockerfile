FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/auth-service.jar auth-service.jar

# Expose the Auth service port
EXPOSE 8085

# Run the jar file
ENTRYPOINT ["java", "-jar", "auth-service.jar"]