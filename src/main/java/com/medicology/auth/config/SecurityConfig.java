package com.medicology.auth.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;



@Configuration

public class SecurityConfig {



    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. Mở cửa hoàn toàn cho Swagger và các file cấu hình của nó
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**" // Thêm dòng này vì lỗi của bạn đang báo ở đây
                ).permitAll()
                
                // 2. Mở cửa cho API Auth của Medicology (kiểm tra kỹ có /medicology ở đầu không)
                .requestMatchers("/**").permitAll() 
                
                // 3. Các request khác mới cần login
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
    
    @Bean
    public OpenAPI medicologyOpenAPI() {
        return new OpenAPI()
                // 1. Định nghĩa cách thức bảo mật là JWT
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // 2. Áp dụng bảo mật này cho tất cả API trong tài liệu
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
    // Các cấu hình SecurityFilterChain khác của bạn...

}
