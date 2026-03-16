package com.medicology.auth.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record ResetPasswordRequestDTO (
    UUID token,
    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự!")
    String newPassword,
    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống!")
    String confirmPassword
){}
