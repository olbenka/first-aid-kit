# Dockerfile for dispatcher module
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /app

COPY target/dispatcher-1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
