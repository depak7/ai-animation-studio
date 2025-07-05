package com.animation.generator.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET = Base64.getEncoder().encodeToString("this_is_a_super_secure_secret_key_of_deepak_any_one_can_access".getBytes());

    public Claims validate(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(Long userId, boolean isGuest) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("isGuest", isGuest)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}
