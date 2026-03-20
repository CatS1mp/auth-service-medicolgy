package com.medicology.auth.controller;

import com.medicology.auth.dto.response.*;
import com.medicology.auth.dto.request.*;
import com.medicology.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerNewUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/oauth")
    public ResponseEntity<LoginResponseDTO> oauthLogin(@Valid@RequestBody OAuthRequestDTO request) {
        return ResponseEntity.ok(authService.processOAuthPostLogin(request));
    }

    //khi click vào token trong mail sẽ gọi endpoint này để xác thực email
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam UUID token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully!");
    }
    // quên mật khẩu
    @PostMapping("/reset/request")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok("Reset link has been sent to your email.");
    }

    @PostMapping("/reset")
    public ResponseEntity<String> confirmReset(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully.");
    }
}