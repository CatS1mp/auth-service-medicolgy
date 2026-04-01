package com.medicology.auth.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequestDTO(
        @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự.")
        String displayName,
        @Size(max = 500, message = "Avatar URL không được vượt quá 500 ký tự.")
        String avatarUrl,
        @Size(max = 500, message = "Bio không được vượt quá 500 ký tự.")
        String bio) {
}
