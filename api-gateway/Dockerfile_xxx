# Sử dụng hình ảnh OpenJDK 21
FROM openjdk:21-jdk-slim

LABEL maintainer="ZimJi <hieuboyfc@gmail.com>"
LABEL version="1.0"
LABEL description="API Gateway"

# Tạo thư mục làm việc
WORKDIR /api-gateway

# Sao chép file JAR từ bước build
COPY build/libs/api-gateway-0.0.1.jar /api-gateway/api-gateway-0.0.1.jar

# Kiểm tra tệp JAR có tồn tại không
RUN ls -l /api-gateway/api-gateway-0.0.1.jar

ENTRYPOINT ["java", "-jar", "/api-gateway/api-gateway-0.0.1.jar"]