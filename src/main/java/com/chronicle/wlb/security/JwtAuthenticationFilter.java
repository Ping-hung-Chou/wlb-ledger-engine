package com.chronicle.wlb.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts every HTTP request and validates the Bearer JWT token.
 *
 * On a valid token, injects a UsernamePasswordAuthenticationToken into the
 * SecurityContext with:
 *   principal   → identity_id (UUID)  — authentication.getName() returns this
 *   authorities → roles from JWT claim — enables @PreAuthorize("hasAuthority('ARCHITECT')")
 *
 * Requests without a valid token are passed through unauthenticated;
 * Spring Security will reject them at the authorization layer if the route requires auth.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (StringUtils.hasText(token) && jwtUtil.isTokenValid(token)) {
            String identityId = jwtUtil.extractIdentityId(token);

            // Convert role strings to GrantedAuthority so @PreAuthorize can evaluate them.
            List<SimpleGrantedAuthority> authorities = jwtUtil.extractRoles(token)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            identityId,    // principal  → identity_id (UUID)
                            null,          // credentials → not needed after token validation
                            authorities    // e.g. [ARCHITECT, USER] — drives @PreAuthorize checks
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw token from the Authorization header.
     * Returns null if the header is absent or not a Bearer token.
     */
    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
