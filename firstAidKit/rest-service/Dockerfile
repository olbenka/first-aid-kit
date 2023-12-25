# Dockerfile for rest-service module
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /app

COPY target/rest-service-1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
