package com.medicology.auth.controller;

import com.medicology.auth.dto.request.UpdateUserStatusRequestDTO;
import com.medicology.auth.dto.response.AdminUserDetailResponseDTO;
import com.medicology.auth.dto.response.UserResponseDTO;
import com.medicology.auth.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers(
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(userManagementService.getAdminUsers(active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUserDetailResponseDTO> getUserDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(userManagementService.getAdminUserDetail(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserStatusRequestDTO request) {
        return ResponseEntity.ok(userManagementService.updateUserStatus(id, request));
    }
}
