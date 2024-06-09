FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY ../../server/build/libs/server-all.jar /app/server.jar
ENTRYPOINT ["java", "-jar", "server.jar"]