# Sử dụng hình ảnh OpenJDK 21
FROM openjdk:21-jdk-slim

LABEL maintainer="ZimJi <hieuboyfc@gmail.com>"
LABEL version="1.0"
LABEL description="API Gateway"

# Tạo thư mục làm việc
WORKDIR /app

# Sao chép file JAR từ bước build
COPY build/libs/api-gateway-0.0.1.jar /app/api-gateway-0.0.1.jar

# Kiểm tra tệp JAR có tồn tại không
RUN ls -l /app/api-gateway-0.0.1.jar

ENTRYPOINT ["java", "-jar", "/app/api-gateway-0.0.1.jar"]