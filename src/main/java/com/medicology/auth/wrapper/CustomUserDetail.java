package com.medicology.auth.wrapper;

import org.springframework.security.core.userdetails.UserDetails;
import com.medicology.auth.entity.User;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Getter;
import java.util.UUID;

@Getter
public class CustomUserDetail implements UserDetails {
    private final UUID id;
    private final String email;
    private final String password;
    private final Boolean isAdmin;
    private final Boolean isVerified;
    private final Boolean isActive;

    public CustomUserDetail(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.isAdmin = user.getIsAdmin();
        this.isVerified = user.getIsVerified();
        this.isActive = user.getIsActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(isAdmin != null && isAdmin ? "ROLE_ADMIN" : "ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return Boolean.TRUE.equals(isActive);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(isVerified);
    }
}
