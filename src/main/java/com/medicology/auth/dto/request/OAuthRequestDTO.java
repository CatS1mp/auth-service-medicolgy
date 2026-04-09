package com.medicology.auth.dto.request;

import jakarta.validation.constraints.*;

public record OAuthRequestDTO(
    @NotBlank(message = "Email không được để trống!")
    String email,
    @NotBlank(message = "Tên hiển thị không được để trống!")
    String name,
    
    String facebookId,
    String googleId
    // Sau này có thể thêm: String appleId...
) {}
