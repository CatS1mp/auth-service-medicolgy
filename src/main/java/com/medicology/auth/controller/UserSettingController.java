package com.medicology.auth.controller;

import com.medicology.auth.dto.request.UpdateUserSettingsRequestDTO;
import com.medicology.auth.dto.response.UserSettingResponseDTO;
import com.medicology.auth.service.UserSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
public class UserSettingController {
    private final UserSettingService userSettingService;

    @GetMapping("/me")
    public ResponseEntity<UserSettingResponseDTO> getCurrentSettings(Authentication authentication) {
        return ResponseEntity.ok(userSettingService.getCurrentSettings(authentication));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserSettingResponseDTO> updateCurrentSettings(
            Authentication authentication,
            @Valid @RequestBody UpdateUserSettingsRequestDTO request) {
        return ResponseEntity.ok(userSettingService.updateCurrentSettings(authentication, request));
    }
}
