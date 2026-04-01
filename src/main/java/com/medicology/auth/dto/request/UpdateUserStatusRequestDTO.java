package com.medicology.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequestDTO(
        @NotNull(message = "Trạng thái active là bắt buộc.")
        Boolean active) {
}
