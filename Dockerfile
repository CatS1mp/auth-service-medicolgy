# Bước 1: Sử dụng hình ảnh JDK để chạy ứng dụng
FROM eclipse-temurin:17-jdk-alpine
# Tạo thư mục app
WORKDIR /app
# Copy file jar đã build từ máy bạn lên (hoặc để Railway tự build)
COPY target/*.jar app.jar
# Lệnh để chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]