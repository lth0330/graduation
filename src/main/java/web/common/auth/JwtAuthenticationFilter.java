package web.common.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization: Bearer ... 헤더에서 JWT를 검증하고 payload를 꺼냅니다.
        // 토큰이 없거나 잘못된 공개 API 요청은 claims가 null인 상태로 다음 필터로 넘어갑니다.
        Claims claims = jwtProvider.extractFromHeader(request.getHeader("Authorization"));

        if (claims != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String role = claims.get("role", String.class);
            String loginId = claims.get("loginId", String.class);

            Map<String, Object> principal = new HashMap<>();
            principal.put("userNo", claims.get("userNo"));
            principal.put("role", role);
            principal.put("apartmentNo", claims.get("apartmentNo"));
            principal.put("loginId", loginId);

            // Spring Security는 ROLE_ 접두사가 붙은 권한을 기준으로 hasRole(...)을 판단합니다.
            // 예: JWT role=APARTMENT_MANAGER -> ROLE_APARTMENT_MANAGER
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 이후 Controller에서 @AuthenticationPrincipal로 principal 정보를 사용할 수 있게 저장합니다.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
