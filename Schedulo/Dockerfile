# Stage 1: Build the application securely using the Maven Wrapper
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix execution permissions for Maven wrapper
RUN chmod +x ./mvnw

# Resolve dependencies (better Docker cache utilization)
RUN ./mvnw dependency:go-offline

# Copy the actual source code and package
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Create a minimal production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/Schedulo-0.0.1-SNAPSHOT.jar app.jar

# Expose the server port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
