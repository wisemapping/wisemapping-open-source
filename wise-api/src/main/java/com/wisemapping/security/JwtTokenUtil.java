package com.wisemapping.security;

import com.wisemapping.model.Account;
import com.wisemapping.service.MetricsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.Serializable;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenUtil implements Serializable {
    final private Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
    public final static String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String DEFAULT_JWT_SECRET = "dlqxKAg685SaKhsQXIMeM=JWCw3bkl3Ei3Tb7LMlnd19oMd66burPNlJ0Po1qguyjgpakQTk2CN3";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void checkJwtSecret() {
        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            logger.warn("######################################################################");
            logger.warn("# WARNING: Using default JWT secret from source code!                #");
            logger.warn("# This is insecure — anyone can forge authentication tokens.         #");
            logger.warn("# Set a unique secret via environment variable APP_JWT_SECRET         #");
            logger.warn("# or property app.jwt.secret before deploying to production.         #");
            logger.warn("######################################################################");
        }
    }

    @Value("${app.jwt.expirationMin}")
    private int jwtExpirationMin;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private MetricsService metricsService;

    public String generateJwtToken(@NotNull final UserDetails user) {
        String token = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMin * 1000L * 60))
                .signWith(key())
                .compact();

        if (token.length() > 3500) {
            logger.error("JWT token size ({}) exceeds safe threshold for browser cookies (3500 bytes). User: {}",
                    token.length(), user.getUsername());
        }
        return token;
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Nullable
    public String extractFromJwtToken(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) key()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateJwtToken(@NotNull String authToken) {
        boolean result = false;
        try {
            Jwts.parser().verifyWith((javax.crypto.SecretKey) key()).build().parse(authToken);
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
        logger.debug("Performing login:" + email);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Track login telemetry
        if (userDetails instanceof Account) {
            Account account = (Account) userDetails;
            metricsService.trackUserLogin(account, "jwt");
        }

        // Add JWT in the HTTP header ...
        final String token = generateJwtToken(userDetails);
        response.addHeader(HttpHeaders.AUTHORIZATION, BEARER_TOKEN_PREFIX + token);

        return token;
    }
}