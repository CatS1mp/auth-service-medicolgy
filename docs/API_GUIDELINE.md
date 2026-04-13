# Auth Service API Guideline

## 1. Mục đích tài liệu

Tài liệu này mô tả endpoint trong `auth-service-medicolgy`, gồm:

- Công dụng của endpoint
- Input request
- Output response
- Màn hình hoặc flow nên sử dụng
- Lưu ý triển khai FE/BE

## 2. Base URL và tài liệu kỹ thuật

- Local: `http://localhost:8080`
- Staging: `Chưa cấu hình riêng trong repo`
- Production: `https://auth-service-medicology-23277f4a5b23.herokuapp.com`

Swagger / OpenAPI:

- `GET /swagger-ui.html`
- `GET /api-docs`
- `GET /` redirect sang `/swagger-ui/index.html`

## 3. Authentication và quy ước chung

### 3.1 Authentication

- Public: `/`, Swagger, toàn bộ `/api/v1/auth/**`
- Yêu cầu JWT Bearer: `/api/v1/users/**`, `/api/v1/profiles/**`, `/api/v1/settings/**`, `/api/v1/oauth/**`, `/api/v1/sessions/**`
- Yêu cầu `ROLE_ADMIN`: `/api/v1/admin/**`

Header chuẩn:

```http
Authorization: Bearer <jwt-token>
Content-Type: application/json
```

### 3.2 Kiểu lỗi

Format lỗi hiện tại:

```json
{
  "status": 400,
  "message": "Mô tả lỗi",
  "timestamp": "2026-04-13T10:30:00"
}
```

Mapping chính:

- `ApiException` giữ nguyên HTTP status do service ném ra
- `IllegalArgumentException` và validation trả `400 Bad Request`
- `AccessDeniedException` trả `403 Forbidden`
- Lỗi chưa bắt riêng trả `500 Internal Server Error`

### 3.3 Quy ước response

- Service này chưa dùng wrapper chung kiểu `ApiResponse<T>`
- Endpoint auth trả `RegisterResponseDTO`, `LoginResponseDTO` hoặc `String`
- Endpoint self-service/admin trả DTO trực tiếp hoặc `204 No Content`
- `POST /api/v1/auth/logout` trả chuỗi thông báo thành công

## 4. Tóm tắt mapping theo màn hình

| Màn hình / flow | Endpoint chính |
| --- | --- |
| Đăng ký | `POST /api/v1/auth/register` |
| Đăng nhập email/password | `POST /api/v1/auth/login` |
| Đăng nhập social | `POST /api/v1/auth/oauth` |
| Verify email | `GET /api/v1/auth/verify?token=...` |
| Quên mật khẩu | `POST /api/v1/auth/reset/request`, `POST /api/v1/auth/reset` |
| Tài khoản của tôi | `GET /api/v1/users/me`, `PATCH /api/v1/users/me` |
| Đổi mật khẩu | `PUT /api/v1/users/me/password` |
| Hồ sơ cá nhân | `GET /api/v1/profiles/me`, `PUT /api/v1/profiles/me` |
| Cài đặt cá nhân | `GET /api/v1/settings/me`, `PATCH /api/v1/settings/me` |
| Tài khoản liên kết | `GET /api/v1/oauth/linked-accounts`, `DELETE /api/v1/oauth/linked-accounts/{provider}` |
| Quản lý phiên đăng nhập | `GET /api/v1/sessions`, `DELETE /api/v1/sessions/{sessionId}` |
| Quản trị người dùng | `GET /api/v1/admin/users`, `GET /api/v1/admin/users/{id}`, `PATCH /api/v1/admin/users/{id}/status` |

## 5. Nhóm API — Authentication

### 5.1 Public auth flow

- `POST /api/v1/auth/register`
  - **Mục đích:** Tạo tài khoản local mới và kích hoạt luồng verify email
  - **Body:** `RegisterRequestDTO` với các field chính như `username`, `email`, `password`, `confirmPassword`
  - **Response:** `201 Created`, `RegisterResponseDTO`
  - **Ghi chú:** Dùng cho form đăng ký email/password
- `POST /api/v1/auth/login`
  - **Mục đích:** Đăng nhập local account
  - **Body:** `LoginRequestDTO` gồm `email`, `password`
  - **Response:** `200 OK`, `LoginResponseDTO` chứa access token, refresh token và thông tin user hiển thị nhanh
  - **Ghi chú:** FE cần lưu refresh token an toàn để dùng cho refresh/logout
- `POST /api/v1/auth/oauth`
  - **Mục đích:** Hoàn tất đăng nhập social sau khi FE lấy được dữ liệu provider
  - **Body:** `OAuthRequestDTO`
  - **Response:** `200 OK`, `LoginResponseDTO`
  - **Ghi chú:** Phù hợp cho flow Google/Facebook login
- `GET /api/v1/auth/verify`
  - **Mục đích:** Xác minh email
  - **Query / Path:** `token=<UUID>`
  - **Response:** `200 OK`, chuỗi `"Email verified successfully!"`
  - **Ghi chú:** Dùng cho link trong email verify
- `POST /api/v1/auth/resend`
  - **Mục đích:** Gửi lại email verify
  - **Query / Path:** `email=<email>`
  - **Response:** `200 OK`, chuỗi xác nhận
- `POST /api/v1/auth/reset/request`
  - **Mục đích:** Tạo reset token và gửi email quên mật khẩu
  - **Query / Path:** `email=<email>`
  - **Response:** `200 OK`, chuỗi xác nhận
- `POST /api/v1/auth/reset`
  - **Mục đích:** Đặt lại mật khẩu bằng token
  - **Body:** `ResetPasswordRequestDTO`
  - **Response:** `200 OK`, chuỗi xác nhận
- `POST /api/v1/auth/logout`
  - **Mục đích:** Thu hồi refresh token hiện tại
  - **Body:** `LogoutRequestDTO` là optional
  - **Response:** `200 OK`, chuỗi xác nhận
  - **Ghi chú:** Controller chấp nhận cả `Authorization` header lẫn body
- `POST /api/v1/auth/refresh`
  - **Mục đích:** Đổi refresh token lấy cặp token mới
  - **Body:** `RefreshTokenRequestDTO`
  - **Response:** `200 OK`, `LoginResponseDTO`

## 6. Nhóm API — Tài khoản và hồ sơ người dùng

### 6.1 Self-service sau đăng nhập

- `GET /api/v1/users/me`
  - **Mục đích:** Lấy thông tin account-level của user hiện tại
  - **Response:** `200 OK`, `UserResponseDTO`
- `PATCH /api/v1/users/me`
  - **Mục đích:** Cập nhật thông tin account-level của user hiện tại
  - **Body:** `UpdateUserRequestDTO`
  - **Response:** `200 OK`, `UserResponseDTO`
- `PUT /api/v1/users/me/password`
  - **Mục đích:** Đổi mật khẩu của user hiện tại
  - **Body:** `SelfChangePasswordRequestDTO`
  - **Response:** `204 No Content`
- `GET /api/v1/profiles/me`
  - **Mục đích:** Lấy profile chi tiết của user hiện tại
  - **Response:** `200 OK`, `UserProfileResponseDTO`
- `PUT /api/v1/profiles/me`
  - **Mục đích:** Cập nhật profile hiện tại
  - **Body:** `UpdateProfileRequestDTO`
  - **Response:** `200 OK`, `UserProfileResponseDTO`
- `GET /api/v1/settings/me`
  - **Mục đích:** Lấy user settings hiện tại
  - **Response:** `200 OK`, `UserSettingResponseDTO`
- `PATCH /api/v1/settings/me`
  - **Mục đích:** Cập nhật một phần settings
  - **Body:** `UpdateUserSettingsRequestDTO`
  - **Response:** `200 OK`, `UserSettingResponseDTO`

## 7. Nhóm API — Linked Accounts và Sessions

### 7.1 Thiết bị và phương thức đăng nhập

- `GET /api/v1/oauth/linked-accounts`
  - **Mục đích:** Liệt kê tài khoản social đã liên kết
  - **Response:** `200 OK`, `List<UserOAuthAccountResponseDTO>`
- `DELETE /api/v1/oauth/linked-accounts/{provider}`
  - **Mục đích:** Gỡ liên kết provider
  - **Query / Path:** `provider` là tên social provider
  - **Response:** `204 No Content`
  - **Ghi chú:** FE nên xử lý riêng trường hợp user không còn phương thức đăng nhập nào khác
- `GET /api/v1/sessions`
  - **Mục đích:** Liệt kê session/refresh token còn hiệu lực của user hiện tại
  - **Response:** `200 OK`, `List<UserSessionResponseDTO>`
- `DELETE /api/v1/sessions/{sessionId}`
  - **Mục đích:** Thu hồi một session cụ thể
  - **Query / Path:** `sessionId=<UUID>`
  - **Response:** `204 No Content`

## 8. Nhóm API — Quản trị người dùng

### 8.1 Admin user management

- `GET /api/v1/admin/users`
  - **Mục đích:** Liệt kê user cho màn hình admin
  - **Query / Path:** `active` là optional filter
  - **Response:** `200 OK`, `List<UserResponseDTO>`
- `GET /api/v1/admin/users/{id}`
  - **Mục đích:** Lấy chi tiết user cho admin
  - **Query / Path:** `id=<UUID>`
  - **Response:** `200 OK`, `AdminUserDetailResponseDTO`
- `PATCH /api/v1/admin/users/{id}/status`
  - **Mục đích:** Khóa/mở khóa tài khoản
  - **Body:** `UpdateUserStatusRequestDTO`
  - **Response:** `200 OK`, `UserResponseDTO`

## 9. Webhook / callback (nếu có)

- Không áp dụng

## 10. Hợp đồng với service khác

- Cấp JWT Bearer để các service backend khác và website dùng cho authn/authz
- Tích hợp OAuth provider qua endpoint `/api/v1/auth/oauth`
- Verify email và reset password phụ thuộc luồng email service / frontend callback URL

---

*Cập nhật lần cuối: 2026-04-13 — Backend team*
