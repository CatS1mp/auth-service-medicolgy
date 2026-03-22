package com.medicology.auth.config;

import com.medicology.auth.security.jwt.JWTDecoder;
import com.medicology.auth.service.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTDecoder jwtDecoder;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userIdentifier; // Thông tin định danh từ JWT (email hoặc username)

        // 1. Kiểm tra JWT có trong header hay không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Trích xuất JWT từ Header (bỏ qua chuỗi "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Trích xuất identifier từ JWT (hiện tại JWTDecoder đang gọi extractEmail để
            // lấy subject)
            userIdentifier = jwtDecoder.extractEmail(jwt);

            // 3. Nếu có thông tin và chưa xác thực thì tiến hành xác thực
            if (userIdentifier != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Chú ý: Cần đảm bảo logic truyền vào loadUserByUsername đồng nhất với việc tạo
                // token.
                // Do JWTTokenProvider đang dùng user.getEmail() làm subject, nhưng
                // loadUserByUsername lại tìm theo Username.
                // Ở đây ta gọi hàm loadUserByUsername như thông thường.
                UserDetails userDetails = userDetailService.loadUserByUsername(userIdentifier);

                // Kiểm tra tính hợp lệ của token
                if (jwtDecoder.isTokenValid(jwt, "access")) {
                    // 4. Khởi tạo đối tượng xác thực
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 5. Báo danh với Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Có thể token hết hạn hoặc sai định dạng, catch exception và để filter tiếp
            // tục (sẽ bị chặn ở các filter sau nếu endpoint requires bảo mật)
            throw new RuntimeException("Lỗi xác thực JWT: " + e.getMessage());
        }

        // 6. Chuyển request đến bộ lọc tiếp theo
        filterChain.doFilter(request, response);
    }
}
