FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY ./build/libs/server-all.jar /app/server.jar

# Copy the JWKS file 
COPY ./certs/jwks.json /app/certs/jwks.json 

# Expose the port
EXPOSE 8080

# Entrypoint: run the jar file
ENTRYPOINT ["java", "-jar", "server.jar"]