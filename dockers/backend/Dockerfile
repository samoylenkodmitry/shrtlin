FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# echo current directory
RUN pwd
# print parent tree
RUN tree ../
COPY ../server/build/libs/server-all.jar /app/server.jar
ENTRYPOINT ["java", "-jar", "server.jar"]