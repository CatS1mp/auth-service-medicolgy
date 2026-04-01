package com.medicology.auth.controller;

import com.medicology.auth.dto.request.SelfChangePasswordRequestDTO;
import com.medicology.auth.dto.request.UpdateUserRequestDTO;
import com.medicology.auth.dto.response.UserResponseDTO;
import com.medicology.auth.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userManagementService.getCurrentUser(authentication));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userManagementService.updateCurrentUser(authentication, request));
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody SelfChangePasswordRequestDTO request) {
        userManagementService.changeCurrentUserPassword(authentication, request);
        return ResponseEntity.noContent().build();
    }
}
