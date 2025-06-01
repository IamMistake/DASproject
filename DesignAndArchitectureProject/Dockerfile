# Use an official OpenJDK image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the application JAR file into the container
COPY target/*.jar app.jar
COPY database/*.csv ./database/

# Expose the port your app runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
