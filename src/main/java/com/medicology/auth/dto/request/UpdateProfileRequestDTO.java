package com.medicology.auth.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequestDTO(
        @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
        String username,
        @Size(max = 100, message = "Họ không được vượt quá 100 ký tự.")
        String lastName,
        @Size(max = 100, message = "Tên không được vượt quá 100 ký tự.")
        String firstName,
        LocalDate dateOfBirth,
        @Size(max = 50, message = "Giới tính không được vượt quá 50 ký tự.")
        String gender,
        @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự.")
        String address,
        @Size(max = 500, message = "Tiểu sử không được vượt quá 500 ký tự.")
        String bio) {
}
