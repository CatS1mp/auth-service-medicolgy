package com.medicology.auth.controller;

import com.medicology.auth.dto.response.UserSessionResponseDTO;
import com.medicology.auth.service.SessionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {
    private final SessionManagementService sessionManagementService;

    @GetMapping
    public ResponseEntity<List<UserSessionResponseDTO>> getSessions(Authentication authentication) {
        return ResponseEntity.ok(sessionManagementService.getCurrentSessions(authentication));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> revokeSession(Authentication authentication, @PathVariable UUID sessionId) {
        sessionManagementService.revokeCurrentSession(authentication, sessionId);
        return ResponseEntity.noContent().build();
    }
}
