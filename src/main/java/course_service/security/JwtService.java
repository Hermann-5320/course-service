package course_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    public String extraireEmail(String token) {
        return extraireClaims(token).getSubject();
    }

    public String extraireRole(String token) {
        return extraireClaims(token).get("role", String.class);
    }

    public Long extraireUserId(String token) {
        return extraireClaims(token).get("userId", Long.class);
    }

    public boolean estValide(String token, String email) {
        return extraireEmail(token).equals(email) && !estExpire(token);
    }

    private boolean estExpire(String token) {
        return extraireClaims(token).getExpiration().before(new Date());
    }

    private Claims extraireClaims(String token) {
        return Jwts.parser()
                .verifyWith(getCle())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getCle() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}