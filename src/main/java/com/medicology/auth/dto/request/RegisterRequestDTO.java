package com.medicology.auth.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequestDTO(
    @NotBlank
    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự!")
    String username,

    @Email(message = "Email không hợp lệ!")
    String email,

    @NotBlank
    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự!")
    String password,

    @NotBlank
    @Size(min = 8, message = "Mật khẩu xác nhận phải từ 8 ký tự!")
    String confirmPassword
) {}