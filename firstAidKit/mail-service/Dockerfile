# Dockerfile for mail-service module
FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /app

COPY target/mail-service-1.0-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
