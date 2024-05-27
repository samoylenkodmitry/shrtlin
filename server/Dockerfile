FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY ./build/libs/server-all.jar /app/server.jar

# Expose the port
EXPOSE 8080

# Entrypoint: run the jar file
ENTRYPOINT ["java", "-jar", "server.jar"]