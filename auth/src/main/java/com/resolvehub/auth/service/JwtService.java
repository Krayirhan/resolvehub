package com.resolvehub.auth.service;

import com.resolvehub.auth.config.JwtProperties;
import com.resolvehub.auth.domain.UserEntity;
import com.resolvehub.common.security.AuthenticatedUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserEntity user) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtProperties.getIssuer())
                .claim("username", user.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return jwtProperties.getAccessTokenMinutes() * 60;
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
    }

    public AuthenticatedUser toAuthenticatedUser(String token) {
        try {
            Claims claims = parse(token).getPayload();
            Long userId = Long.parseLong(claims.getSubject());
            String username = claims.get("username", String.class);
            List<?> roleList = claims.get("roles", List.class);
            Set<String> roles = roleList == null ? Set.of() :
                    roleList.stream().map(String::valueOf).collect(Collectors.toSet());
            return new AuthenticatedUser(userId, username, roles);
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }
}
