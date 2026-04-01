# Auth Service API Guide

## 1. Tổng quan

Tài liệu này mô tả tác dụng của từng endpoint trong auth service, gồm:

- Authentication endpoints
- User self-service endpoints
- Admin user management endpoints
- Profile, settings, OAuth linked accounts, sessions

Base path chính:

```text
/api/v1
```

Xác thực:

- Các endpoint `/api/v1/auth/**` là public, trừ một số flow cần token trong header.
- Các endpoint còn lại cần `Authorization: Bearer <access_token>`.
- Các endpoint `/api/v1/admin/**` cần quyền `ROLE_ADMIN`.

## 2. Response và Error

Phần lớn endpoint trả JSON object domain-specific.

Khi lỗi, service trả về object dạng:

```json
{
  "status": 400,
  "message": "Mo ta loi",
  "timestamp": "2026-04-02T03:00:00"
}
```

Một số mã lỗi thường gặp:

- `400 Bad Request`: request sai dữ liệu, validate fail, token reset sai, username trùng
- `401 Unauthorized`: chưa đăng nhập hoặc token không hợp lệ
- `403 Forbidden`: không đủ quyền hoặc tài khoản bị khóa/chưa verify
- `404 Not Found`: không tìm thấy user, session, linked account

## 3. Authentication Endpoints

### `POST /api/v1/auth/register`

Tác dụng:

- Tạo tài khoản mới bằng email/password
- Sinh verification token và gửi email xác minh

Khi dùng:

- Dùng cho màn hình đăng ký tài khoản bằng email

Body chính:

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

Kết quả:

- `201 Created`
- Trả về email vừa đăng ký

### `POST /api/v1/auth/login`

Tác dụng:

- Đăng nhập bằng email/password
- Trả access token và refresh token
- Kèm thông tin profile header để FE hiển thị nhanh

Khi dùng:

- Dùng cho form đăng nhập thường

Body chính:

```json
{
  "email": "john@example.com",
  "password": "Password123!"
}
```

Kết quả:

- `200 OK`
- Trả `accessToken`, `refreshToken`, `expiresIn`, `userProfile`

### `POST /api/v1/auth/oauth`

Tác dụng:

- Hoàn tất đăng nhập OAuth sau khi FE đã xác thực với provider
- Tạo user nếu chưa tồn tại
- Tạo profile/settings mặc định nếu cần
- Lưu liên kết Google/Facebook

Khi dùng:

- Dùng sau khi FE lấy được thông tin user từ Google/Facebook

### `GET /api/v1/auth/verify?token=<uuid>`

Tác dụng:

- Xác minh email từ link trong mail
- Đánh dấu user đã verify
- Tạo `user_profile` nếu chưa có

Khi dùng:

- Dùng cho luồng click link xác minh tài khoản

### `POST /api/v1/auth/resend?email=<email>`

Tác dụng:

- Gửi lại email verification cho tài khoản chưa verify

Khi dùng:

- Dùng khi user chưa nhận được mail hoặc mail hết hạn

### `POST /api/v1/auth/reset/request?email=<email>`

Tác dụng:

- Tạo reset token và gửi email reset mật khẩu

Khi dùng:

- Dùng cho màn hình quên mật khẩu

### `POST /api/v1/auth/reset`

Tác dụng:

- Reset mật khẩu bằng reset token

Body chính:

```json
{
  "token": "uuid-token",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

### `POST /api/v1/auth/logout`

Tác dụng:

- Thu hồi refresh token hiện tại nếu FE gửi lên

Khi dùng:

- Dùng khi user bấm đăng xuất

Body có thể gửi:

```json
{
  "refreshToken": "refresh-token-string"
}
```

### `POST /api/v1/auth/refresh`

Tác dụng:

- Đổi refresh token lấy access token mới
- Rotate refresh token: token cũ bị revoke, token mới được cấp lại

Khi dùng:

- Dùng khi access token hết hạn

Body:

```json
{
  "refreshToken": "refresh-token-string"
}
```

## 4. User Self-Service Endpoints

### `GET /api/v1/users/me`

Tác dụng:

- Lấy thông tin user hiện tại
- Trả về các field có tính account-level như email, username, status, role, createdAt

Khi dùng:

- Dùng cho màn hình account overview

### `PATCH /api/v1/users/me`

Tác dụng:

- Cập nhật thông tin account-level của user hiện tại
- Hiện tại phù hợp cho `username`, `dateOfBirth`, `location`

Khi dùng:

- Dùng khi user sửa thông tin cá nhân cơ bản

Body mẫu:

```json
{
  "username": "john_updated",
  "dateOfBirth": "2000-01-15",
  "location": "Ho Chi Minh City"
}
```

### `PUT /api/v1/users/me/password`

Tác dụng:

- Đổi mật khẩu cho user đang đăng nhập
- Kiểm tra mật khẩu hiện tại trước khi đổi

Khi dùng:

- Dùng trong trang account/security

Body mẫu:

```json
{
  "currentPassword": "Password123!",
  "newPassword": "NewPassword123!",
  "confirmNewPassword": "NewPassword123!"
}
```

Kết quả:

- `204 No Content` nếu thành công

## 5. Profile Endpoints

### `GET /api/v1/profiles/me`

Tác dụng:

- Lấy user profile hiện tại
- Chứa các field mang tính hiển thị như `displayName`, `avatarUrl`, `bio`

Khi dùng:

- Dùng cho profile page hoặc header/profile drawer

### `PUT /api/v1/profiles/me`

Tác dụng:

- Cập nhật profile hiện tại
- Tự động tạo profile nếu user chưa có bản ghi profile

Body mẫu:

```json
{
  "displayName": "John Doe",
  "avatarUrl": "https://cdn.example.com/avatar.jpg",
  "bio": "Doctor and lifelong learner"
}
```

## 6. Settings Endpoints

### `GET /api/v1/settings/me`

Tác dụng:

- Lấy user settings hiện tại
- Trả về các tùy chọn thông báo, reminder, theme, daily goal

Khi dùng:

- Dùng cho trang settings/preferences

### `PATCH /api/v1/settings/me`

Tác dụng:

- Cập nhật một phần settings hiện tại
- Tự động tạo settings mặc định nếu user chưa có

Body mẫu:

```json
{
  "notificationEnabled": true,
  "dailyReminderTime": "08:30:00",
  "emailNotifications": true,
  "pushNotifications": false,
  "themePreference": "dark",
  "dailyGoalCourses": 3
}
```

## 7. OAuth Linked Account Endpoints

### `GET /api/v1/oauth/linked-accounts`

Tác dụng:

- Liệt kê các tài khoản OAuth đã liên kết với user hiện tại
- Hiện tại hỗ trợ `google` và `facebook`

Khi dùng:

- Dùng cho màn hình "Connected accounts"

Response ý nghĩa:

- Mỗi item cho biết `provider`, `providerUserId`, `providerEmail`

### `DELETE /api/v1/oauth/linked-accounts/{provider}`

Tác dụng:

- Gỡ liên kết OAuth theo provider

Provider hợp lệ:

- `google`
- `facebook`

Lưu ý nghiệp vụ:

- Không cho gỡ phương thức đăng nhập cuối cùng nếu tài khoản không có mật khẩu local

Kết quả:

- `204 No Content` nếu thành công

## 8. Session Endpoints

### `GET /api/v1/sessions`

Tác dụng:

- Liệt kê các session đang hoạt động của user hiện tại
- Về bản chất là danh sách refresh token chưa bị revoke và chưa hết hạn

Khi dùng:

- Dùng cho màn hình "Manage devices" hoặc "Logged in sessions"

Response ý nghĩa:

- `id`: session id để FE sử dụng khi revoke
- `createdAt`: thời điểm tạo refresh token
- `expiresAt`: thời điểm hết hạn
- `revoked`: trạng thái revoke
- `tokenPreview`: một đoạn ngắn để phân biệt session

### `DELETE /api/v1/sessions/{sessionId}`

Tác dụng:

- Thu hồi một session cụ thể của user hiện tại
- Chỉ thu hồi session thuộc chính user đang đăng nhập

Khi dùng:

- Dùng khi user muốn đăng xuất thiết bị khác

Kết quả:

- `204 No Content` nếu thành công

## 9. Admin User Management Endpoints

### `GET /api/v1/admin/users`

Tác dụng:

- Liệt kê user cho admin
- Có thể filter theo trạng thái `active`

Query hỗ trợ:

- `active=true`
- `active=false`

Khi dùng:

- Dùng cho dashboard quản trị user

### `GET /api/v1/admin/users/{id}`

Tác dụng:

- Lấy chi tiết 1 user cho admin
- Gồm:
  - thong tin user
  - profile
  - settings
  - linked OAuth accounts
  - active sessions

Khi dùng:

- Dùng cho trang user detail trong admin panel

### `PATCH /api/v1/admin/users/{id}/status`

Tác dụng:

- Khóa/mở khóa tài khoản user qua field `active`

Body mẫu:

```json
{
  "active": false
}
```

Khi dùng:

- Dùng khi admin cần tạm khóa hoặc mở lại tài khoản

Lưu ý:

- User bị `active=false` sẽ không đăng nhập mới được và không refresh session tiếp

## 10. Gợi ý mapping màn hình FE

- Đăng ký: `POST /auth/register`
- Đăng nhập: `POST /auth/login`
- Verify email: `GET /auth/verify`
- Quên mật khẩu: `POST /auth/reset/request`, `POST /auth/reset`
- Account overview: `GET /users/me`
- Edit account basics: `PATCH /users/me`
- Đổi mật khẩu: `PUT /users/me/password`
- Edit public profile: `GET /profiles/me`, `PUT /profiles/me`
- Preferences: `GET /settings/me`, `PATCH /settings/me`
- Connected accounts: `GET /oauth/linked-accounts`, `DELETE /oauth/linked-accounts/{provider}`
- Manage sessions/devices: `GET /sessions`, `DELETE /sessions/{sessionId}`
- Admin users table: `GET /admin/users`
- Admin user detail: `GET /admin/users/{id}`
- Admin lock/unlock account: `PATCH /admin/users/{id}/status`

## 11. Lưu ý thực tế cho frontend

- Lưu `refreshToken` an toàn vì logout và refresh đều cần đến nó.
- Nếu endpoint trả `403` với message tài khoản bị khóa, FE nên force logout và hiển thị thông báo rõ ràng.
- Nếu user OAuth chưa có mật khẩu local, không nên hiện form đổi mật khẩu theo cách thông thường.
- `sessions` hiện đang đại diện cho refresh token, chưa phân biệt chi tiết browser/device/IP.
