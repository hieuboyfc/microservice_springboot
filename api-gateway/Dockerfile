FROM openjdk:21-jdk-alpine

LABEL maintainer="ZimJi <hieuboyfc@gmail.com>"
LABEL version="1.0"
LABEL description="API Gateway"

WORKDIR /app

COPY build/libs/api-gateway.jar /app/api-gateway.jar

ENTRYPOINT ["java", "-jar", "/app/api-gateway.jar"]
