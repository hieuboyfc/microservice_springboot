# Bước 1: Sử dụng hình ảnh OpenJDK cơ bản
FROM openjdk:21-jdk-slim AS build

# Tạo thư mục làm việc
WORKDIR /api-gateway

# Cài đặt các công cụ cần thiết
RUN apt-get update && apt-get install -y unzip curl

# Cài đặt Gradle
RUN mkdir /opt/gradle && \
    curl -sSL https://services.gradle.org/distributions/gradle-8.8-bin.zip -o gradle.zip && \
    unzip gradle.zip -d /opt/gradle && \
    rm gradle.zip && \
    ln -s /opt/gradle/gradle-8.8/bin/gradle /usr/bin/gradle && \
    gradle --version

# Sao chép mã nguồn
COPY . .

# Xây dựng ứng dụng
RUN gradle build --no-daemon --info

# Bước 2: Tạo hình ảnh runtime
FROM openjdk:21-jdk-slim

LABEL maintainer="ZimJi <hieuboyfc@gmail.com>"
LABEL version="1.0"
LABEL description="API Gateway"

# Tạo thư mục làm việc
WORKDIR /api-gateway

# Sao chép file JAR từ bước build
COPY --from=build /api-gateway/build/libs/api-gateway-0.0.1.jar /api-gateway/api-gateway-0.0.1.jar

# Kiểm tra tệp JAR có tồn tại không
RUN ls -l /api-gateway/api-gateway-0.0.1.jar

ENTRYPOINT ["java", "-jar", "/api-gateway/api-gateway-0.0.1.jar"]

# docker build -t hieuboyfc/api-gateway:latest .
# docker login
# docker push hieuboyfc/api-gateway:latest