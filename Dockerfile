# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Install dependencies for downloading flatc
RUN apt-get update && apt-get install -y wget unzip

# Download FlatBuffers compiler (flatc) for Linux
# Using version 23.5.26 to match pom.xml
RUN wget https://github.com/google/flatbuffers/releases/download/v23.5.26/Linux.flatc.binary.clang++-12.zip -O flatc.zip && \
    unzip flatc.zip -d /usr/local/bin && \
    chmod +x /usr/local/bin/flatc && \
    rm flatc.zip

COPY pom.xml .
COPY src ./src
COPY tools ./tools

# Run Maven build with the Linux flatc executable path
RUN mvn clean package -DskipTests -Dflatc.executable=/usr/local/bin/flatc

# Runtime stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN groupadd -r eternity && useradd -r -g eternity eternity

# Copy the built artifact
COPY --from=build /app/target/eternity-1.0-SNAPSHOT.jar app.jar

# Expose ports (Server port, gRPC port if different)
EXPOSE 8080 50051

# Set ownership
RUN chown -R eternity:eternity /app

USER eternity

# Environment variables
ENV SERVER_PORT=8080
ENV REDIS_HOST=localhost
ENV REDIS_PORT=6379

ENTRYPOINT ["java", "-jar", "app.jar"]
