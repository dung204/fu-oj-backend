# ====== Build stage ======
FROM eclipse-temurin:21-jdk-alpine as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and config files
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# Make mvnw executable & download dependencies (cached)
RUN chmod +x ./mvnw \
    && ./mvnw -B dependency:resolve-plugins dependency:resolve

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests \
    -Dmaven.compiler.debug=false \
    -Dmaven.compiler.debuglevel=none


# ====== Runtime stage ======
FROM eclipse-temurin:21-jre-alpine

# Create app user
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Prepare logs directory
RUN mkdir -p /app/logs && chown -R spring:spring /app

# Copy jar from builder
COPY --from=builder /app/target/*.jar app.jar
COPY .env ./
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring

# Expose app port
EXPOSE 4000
# Env variables
ENV SERVER_PORT=4000
# Xms256m: initial memory allocation pool
# Xmx512m: maximum memory allocation pool
ENV JVM_OPTS="-Xms256m -Xmx2048m -XX:+UseG1GC -Dspring.profiles.active=prod"

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT}/api/v1/auth/test || exit 1

# Start app
ENTRYPOINT exec java $JVM_OPTS -jar app.jar
