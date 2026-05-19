package com.medicology.auth.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserRequestDTO(
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
        String username) {
}
