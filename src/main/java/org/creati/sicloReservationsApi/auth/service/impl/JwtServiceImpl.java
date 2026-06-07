package org.creati.sicloReservationsApi.auth.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.creati.sicloReservationsApi.auth.dto.UserDto;
import org.creati.sicloReservationsApi.auth.exception.GenerationTokenException;
import org.creati.sicloReservationsApi.auth.model.User;
import org.creati.sicloReservationsApi.auth.service.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final String secretKey;
    private final Long jwtExpiration;
    private final ObjectMapper objectMapper;

    public JwtServiceImpl(
            @Value("${jwt.secret}") final String secretKey,
            @Value("${jwt.expiration}") final Long jwtExpiration,
            final ObjectMapper objectMapper) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
        this.objectMapper = objectMapper;
    }


    public <T> T extractClaim(String token, String claimName, Class<T> type) {
        // Get Signing key
        byte[] keyBytes = secretKey.getBytes();
        SecretKey signKey = Keys.hmacShaKeyFor(keyBytes);

        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object value = claims.get(claimName);

        if (type == Instant.class) {
            if (value instanceof Date date) {
                return type.cast(date.toInstant());
            } else if (value instanceof Long epochSeconds) {
                return type.cast(Instant.ofEpochSecond(epochSeconds));
            } else if (value instanceof Integer epochSeconds) {
                return type.cast(Instant.ofEpochSecond(epochSeconds.longValue()));
            }
        }

        return type.cast(value);


    }

    public String generateToken(User user) throws GenerationTokenException {
        try {
            // Build dtos
            Map<String, Object> extraClaims = new HashMap<>();
            UserDto userInfo = new UserDto(user);
            String userInfoJson = objectMapper.writeValueAsString(userInfo);

            // Set claims
            extraClaims.put("userInfo", userInfoJson);
            extraClaims.put("userId", user.getId());

            // Get Signing key
            byte[] keyBytes = secretKey.getBytes();
            SecretKey signKey = Keys.hmacShaKeyFor(keyBytes);

            // Build token
            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(user.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                    .signWith(signKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (JsonProcessingException e) {
            log.error("Error serializing user info for JWT token", e);
            throw new GenerationTokenException("Error generating token", e);
        }
    }


    public boolean isTokenValid(String token, UserDetails user) {

        final String username = extractClaim(token, "sub", String.class);
        final Instant expiration = extractClaim(token, "exp", Instant.class);
        final boolean isTokenExpired = expiration.isBefore(Instant.now());

        return (username.equals(user.getUsername())) && !isTokenExpired;
    }

}
