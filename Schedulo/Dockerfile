# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper files first (cache layer — only rebuilt if pom.xml changes)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x ./mvnw

# Download dependencies (separate layer for Docker cache efficiency)
RUN ./mvnw dependency:go-offline -B

# Copy source and build (skip tests — tests need DB connection)
COPY src src
RUN ./mvnw clean package -Dmaven.test.skip=true -B

# ── Stage 2: Production image ─────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy built JAR
COPY --from=build /app/target/Schedulo-0.0.1-SNAPSHOT.jar app.jar

# Set ownership
RUN chown spring:spring app.jar
USER spring

# Render uses PORT env var — pass it through to JVM
EXPOSE 8080

# JVM tuning for container memory limits (important on Render free tier)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
