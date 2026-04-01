package com.medicology.auth.config;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor

public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthFilter; // Filter bạn đã viết
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                // 1. Mở cửa hoàn toàn cho Swagger và các file cấu hình của nó
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/v3/api-docs",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html",
                                                                "/api-docs/**" // Thêm dòng này vì lỗi của bạn đang báo
                                                // ở đây
                                                ).permitAll()

                                                // 2. Mở cửa cho API Auth của Medicology (kiểm tra kỹ có /medicology ở
                                                // đầu
                                                // không)
                                                .requestMatchers("/api/v1/auth/**").permitAll()

                                                // 3. Các request khác mới cần login
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // 4. Khai báo bộ xác thực (Dùng UserDetailsService + PasswordEncoder)
                                .authenticationProvider(authenticationProvider)

                                // 5. CHÈN FILTER: Chạy JwtAuthFilter TRƯỚC UsernamePasswordAuthenticationFilter
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public OpenAPI medicologyOpenAPI() {
                // Định nghĩa Server để tránh lỗi Mixed Content (HTTP vs HTTPS)
                Server productionServer = new Server()
                                .url("https://auth-service-medicology-production.up.railway.app")
                                .description("Server chính thức trên Railway");

                Server localServer = new Server()
                                .url("http://localhost:8080")
                                .description("Server chạy ở Local");

                return new OpenAPI()
                                // 1. Thêm danh sách Server vào đây
                                .servers(List.of(productionServer, localServer))

                                // 2. Giữ nguyên phần định nghĩa JWT của bạn
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .scheme("bearer")
                                                                                .bearerFormat("JWT")))

                                // 3. Giữ nguyên việc áp dụng bảo mật
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
        }// Các cấu hình SecurityFilterChain khác của bạn...

}
