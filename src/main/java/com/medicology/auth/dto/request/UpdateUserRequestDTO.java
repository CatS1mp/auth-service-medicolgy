package com.medicology.auth.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequestDTO(
        @Size(min = 3, max = 50, message = "Username phải từ 3 đến 50 ký tự.")
        String username,
        LocalDate dateOfBirth,
        @Size(max = 255, message = "Location không được vượt quá 255 ký tự.")
        String location) {
}
