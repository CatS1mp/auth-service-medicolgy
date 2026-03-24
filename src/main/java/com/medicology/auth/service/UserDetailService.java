package com.medicology.auth.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.medicology.auth.entity.User;
import com.medicology.auth.wrapper.CustomUserDetail;
import com.medicology.auth.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public CustomUserDetail loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
        return new CustomUserDetail(user);
    }
}
