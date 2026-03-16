package com.medicology.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";

    // 2. Thời hạn (để Frontend chủ động)
    private Long expiresIn; 

    // 3. Thông tin User để hiển thị UI nhanh
    private UserProfileHeaderResponseDTO userProfile;
}
