FROM adoptopenjdk/openjdk11:ubi
WORKDIR /firstAidKit

COPY common-rabbitmq/target/common-rabbitmq-1.0-SNAPSHOT.jar /firstAidKit
COPY common-jpa/target/common-jpa-1.0-SNAPSHOT.jar /firstAidKit
COPY common-utils/target/common-utils-1.0-SNAPSHOT.jar /firstAidKit
COPY dispatcher/target/dispatcher-1.0-SNAPSHOT.jar /firstAidKit
COPY mail-service/target/mail-service-1.0-SNAPSHOT.jar /firstAidKit
COPY node/target/node-1.0-SNAPSHOT.jar /firstAidKit
COPY rest-service/target/rest-service-1.0-SNAPSHOT.jar /firstAidKit
