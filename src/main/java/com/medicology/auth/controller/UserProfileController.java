package com.medicology.auth.controller;

import com.medicology.auth.dto.request.UpdateProfileRequestDTO;
import com.medicology.auth.dto.response.UserProfileResponseDTO;
import com.medicology.auth.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getCurrentProfile(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateCurrentProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userProfileService.updateCurrentProfile(authentication, request));
    }
}
