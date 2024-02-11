package com.wisemapping.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil implements Serializable {
    final private Logger logger = LogManager.getLogger();
    public final static String BEARER_TOKEN_PREFIX = "Bearer ";


    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expirationMin}")
    private int jwtExpirationMin;

    @Autowired
    private UserDetailsService userDetailsService;


    public String generateJwtToken(@NotNull final UserDetails user) {
        return Jwts.builder()
                .setSubject((user.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMin * 1000L * 60))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }


    @Nullable
    public String extractFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(@NotNull String authToken) {
        boolean result = false;
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            result = true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        logger.trace("Is JWT token valid:" + result);
        return result;
    }

    @NotNull
    public String doLogin(@NotNull HttpServletResponse response, @NotNull String email) {
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Add JWT in the HTTP header ...
        final String token = generateJwtToken(userDetails);
        response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + token);

        return token;
    }
}