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

    @Value("${jwt.secret:graduation-project-jwt-secret-key-must-be-at-least-32-bytes}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

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
