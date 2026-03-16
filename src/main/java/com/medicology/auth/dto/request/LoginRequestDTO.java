package com.medicology.auth.dto.request;

import jakarta.validation.constraints.*;

public record LoginRequestDTO(
    @Email(message = "Email không hợp lệ!")
    String email,

    @NotBlank(message = "Mật khẩu không được để trống!")
    String password
) {}
