package com.medicology.auth.controller;

import com.medicology.auth.dto.request.UpdateProfileRequestDTO;
import com.medicology.auth.dto.response.UserProfileResponseDTO;
import com.medicology.auth.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentProfile(Authentication authentication) {
        return ResponseEntity.ok(userProfileService.getCurrentProfile(authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDTO> getProfileById(
            Authentication authentication,
            @PathVariable UUID id) {
        return ResponseEntity.ok(userProfileService.getProfileByUserId(authentication, id));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> updateCurrentProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userProfileService.updateCurrentProfile(authentication, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponseDTO> updateProfileById(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userProfileService.updateProfileByUserId(authentication, id, request));
    }
}
