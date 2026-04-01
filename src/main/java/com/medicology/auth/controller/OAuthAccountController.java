package com.medicology.auth.controller;

import com.medicology.auth.dto.response.UserOAuthAccountResponseDTO;
import com.medicology.auth.service.OAuthAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/oauth/linked-accounts")
@RequiredArgsConstructor
public class OAuthAccountController {
    private final OAuthAccountService oauthAccountService;

    @GetMapping
    public ResponseEntity<List<UserOAuthAccountResponseDTO>> getLinkedAccounts(Authentication authentication) {
        return ResponseEntity.ok(oauthAccountService.getCurrentLinkedAccounts(authentication));
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> unlinkProvider(Authentication authentication, @PathVariable String provider) {
        oauthAccountService.unlinkCurrentProvider(authentication, provider);
        return ResponseEntity.noContent().build();
    }
}
