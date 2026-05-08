package com.atta.PolyPaste.jwt;

import com.atta.PolyPaste.repository.UserRepository;
import com.atta.PolyPaste.repository.VkUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    final private VkUserRepository vkUserRepository;

    public JwtProvider(VkUserRepository vkUserRepository) {
        this.vkUserRepository = vkUserRepository;
    }

    public String generateToken(Long id, String firstName, String lastName) {
        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .claim("role", "USER")
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .setIssuer("poly_paste")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(24, ChronoUnit.HOURS)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractVkUserId(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractFirstName(String token) {
        return extractAllClaims(token).get("firstName", String.class);
    }

    public String extractLastName(String token) {
        return extractAllClaims(token).get("lastName", String.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractJwt(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            boolean isExpired = claims.getExpiration().before(new Date());
            boolean exists = vkUserRepository.existsByVkId(Long.valueOf(claims.getSubject()));
            return !isExpired && exists;
        } catch (Exception e) {
            return false;
        }
    }
}
