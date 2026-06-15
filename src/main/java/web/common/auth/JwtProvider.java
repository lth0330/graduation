package web.common.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import web.common.type.UserRole;

@Component
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    // 로그인 성공 시 프론트/앱에 내려줄 JWT를 생성합니다.
    // userNo, role, apartmentNo는 이후 권한 검사와 자기 아파트 데이터 조회에 사용됩니다.
    public String generateToken(Integer userNo, UserRole role, Integer apartmentNo, String loginId) {
        return Jwts.builder()
                .subject(String.valueOf(userNo))
                .claim("userNo", userNo)
                .claim("role", role.name())
                .claim("apartmentNo", apartmentNo)
                .claim("loginId", loginId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // 토큰 서명과 만료 시간을 검증합니다. 실패하면 예외를 밖으로 던지지 않고 null을 반환합니다.
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    // Authorization 헤더는 "Bearer 토큰값" 형식이므로 앞의 Bearer 문자열을 제거한 뒤 검증합니다.
    public Claims extractFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return validateToken(authorizationHeader.substring(7));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
