package com.wisemapping.filter;

import com.wisemapping.security.JwtTokenUtil;
import com.wisemapping.security.UserDetails;
import com.wisemapping.security.UserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.wisemapping.security.JwtTokenUtil.BEARER_TOKEN_PREFIX;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    final private static Logger logger = LogManager.getLogger();

    @Override
    protected void doFilterInternal(@NotNull final HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        final Optional<String> token = getJwtTokenFromRequest(request);

        if (token.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Extract email from token ...
            final Optional<String> email = extractEmailFromToken(token.get());

            if (email.isPresent() && jwtTokenUtil.validateJwtToken(token.get())) {
                // Is it an existing user ?
                try {
                    final UserDetails userDetails = userDetailsService.loadUserByUsername(email.get());
                    final UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (UsernameNotFoundException e) {
                    logger.trace("User " + email.get() + " could not be found");
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractEmailFromToken(final @NotNull String token) {
        Optional<String> result = Optional.empty();
        try {
            result = Optional.ofNullable(jwtTokenUtil.extractFromJwtToken(token));
        } catch (Exception e) {
            // Handle token extraction/validation errors
            logger.debug("Error extracting email from token: " + e.getMessage());
        }
        logger.trace("JWT token email:" + result);
        return result;
    }

    private static Optional<String> getJwtTokenFromRequest(@NotNull HttpServletRequest request) {
        Optional<String> result = Optional.empty();

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null) {
            if (authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
                logger.trace("JWT Bearer token found.");
                final String token = authorizationHeader.substring(BEARER_TOKEN_PREFIX.length());
                result = Optional.of(token);
            }
        }
        return result;
    }
}
