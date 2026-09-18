package com.smartmetrix.backend.security;

import com.smartmetrix.backend.user.User;
import com.smartmetrix.backend.user.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(
            UserRepository userRepository,
            @Value("${app.jwt.secret}") String secret) {

        this.userRepository = userRepository;

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 characters"
            );
        }

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader =
                request.getHeader("Authorization");

        // No Authorization header
        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authHeader.substring(7);

        try {

            // Validate and read JWT
            Claims claims =
                    Jwts.parser()
                            .verifyWith(secretKey)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            String email =
                    claims.getSubject();

            // Find user from database
            User user =
                    userRepository
                            .findByEmail(email)
                            .orElse(null);

            // Check user
            if (user != null &&
                    user.getRole() != null &&
                    "ACTIVE".equalsIgnoreCase(
                            user.getStatus())) {

                String role =
                        user.getRole()
                                .trim()
                                .toUpperCase();

                List<SimpleGrantedAuthority>
                        authorities =
                        new ArrayList<>();

                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role
                        )
                );

                /*
                 * FIX:
                 * Store user ID in credentials.
                 *
                 * InspectionService and AuditLogService
                 * read the current user ID using:
                 *
                 * authentication.getCredentials()
                 */
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getEmail(),
                                user.getId(),
                                authorities
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }

        } catch (Exception e) {

            SecurityContextHolder
                    .clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}