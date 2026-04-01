package com.medicology.auth.dto.response;

import java.util.List;

public record AdminUserDetailResponseDTO(
        UserResponseDTO user,
        UserProfileResponseDTO profile,
        UserSettingResponseDTO settings,
        List<UserOAuthAccountResponseDTO> linkedAccounts,
        List<UserSessionResponseDTO> sessions) {
}
