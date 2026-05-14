# Auth Service — Medicology

Dịch vụ xác thực/ủy quyền cho hệ thống Medicology. Cung cấp đăng nhập, phát hành/refresh JWT, phân quyền theo vai trò, và các chính sách bảo mật cốt lõi.

## Công nghệ
- Spring Boot 3.3, Java 17
- Spring Security, JWT
- Spring Data JPA (RDBMS)

## Tính năng chính
- Quản lý người dùng/nhóm/quyền truy cập (RBAC theo dự án).
- Cấp phát, xác minh, và refresh Access Token (JWT).
- Chính sách mật khẩu, giới hạn/throttle đăng nhập, audit sự kiện bảo mật (tùy cấu hình).

## Yêu cầu
- Java 17
- Maven 3.9+
- CSDL quan hệ (cấu hình qua `spring.datasource.*` hoặc biến môi trường tương ứng)

## Cấu hình môi trường (ví dụ)
- `SPRING_PROFILES_ACTIVE=dev`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medicology_auth` (hoặc DB khác)
- `SPRING_DATASOURCE_USERNAME=...`
- `SPRING_DATASOURCE_PASSWORD=...`
- Chìa khoá JWT (tuỳ thuật toán):
  - HMAC: `JWT_SECRET=...`
  - RSA/ECDSA: `JWT_PUBLIC_KEY=...`, `JWT_PRIVATE_KEY=...`
- Tuỳ chọn: `SERVER_PORT=8081`

## Chạy local
```bash
mvn spring-boot:run
```
Hoặc build JAR:
```bash
mvn clean package -DskipTests
java -jar target/*.jar
```

## Kiểm thử
```bash
mvn verify -q
```

## Bảo mật & tuân thủ
- Không ghi log thông tin nhạy cảm/PHI; không đưa PHI vào URL, thông điệp lỗi, hoặc storage trình duyệt.
- Áp dụng nguyên tắc quyền tối thiểu (least privilege) trên roles/scopes.
- Xem xét throttle/chặn IP theo chính sách khi phát hiện hành vi bất thường.

## Tài liệu API
- Khi bật OpenAPI (springdoc), có thể truy cập Swagger UI và spec OpenAPI. Đường dẫn tùy cấu hình dự án.

## Ghi chú
- API Gateway sẽ xác minh JWT ở rìa; các service phía sau uỷ quyền dựa trên claims/roles.
