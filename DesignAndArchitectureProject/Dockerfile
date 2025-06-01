# ---------- Build Stage ----------
FROM maven:3.8.6-openjdk-18-slim as builder

ARG BUILD_DIR=/tmp/build
WORKDIR ${BUILD_DIR}

# Copy pom.xml and prefetch dependencies
COPY pom.xml .
RUN mvn -Dmaven.repo.local=${BUILD_DIR}/.m2 dependency:go-offline

# Copy the full source code
COPY src ./src/

# Build the application
RUN mvn -Dmaven.repo.local=${BUILD_DIR}/.m2 -DskipTests package

# ---------- Runtime Stage ----------
FROM openjdk:18-slim as runner

ARG BUILD_DIR=/tmp/build
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder ${BUILD_DIR}/target/*.jar app.jar

# Optional: Drop privileges (only if the app doesn’t need root)
# RUN useradd -r -u 1001 springuser
# USER springuser

EXPOSE 8080

# Start the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
