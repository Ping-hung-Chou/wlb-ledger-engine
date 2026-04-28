package com.chronicle.wlb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * Stateless JWT utility — responsible for token generation and validation only.
 *
 * Token design:
 *   subject  → identity_id (UUID): used as the SecurityContext principal name
 *              so that authentication.getName() always returns a valid DB FK.
 *   "roles"  → List<String> claim (e.g. ["USER", "ARCHITECT"]): used by
 *              JwtAuthenticationFilter to populate GrantedAuthority, enabling
 *              @PreAuthorize("hasAuthority('ARCHITECT')") checks.
 */
@Component
public class JwtUtil {

    private static final String ROLES_CLAIM = "roles";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Issues a signed JWT token embedding the user's identity_id and roles.
     *
     * @param identityId the UUID primary key of the authenticated user (JWT subject)
     * @param roles      list of role strings parsed from the Identity.role JSON field
     * @return compact, URL-safe JWT string
     */
    public String generateToken(String identityId, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
                .subject(identityId)                          // principal = identity_id
                .claim(ROLES_CLAIM, roles)                    // authority list for @PreAuthorize
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the identity_id (subject) from a valid JWT token.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return the identity_id stored as the token subject
     */
    public String extractIdentityId(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the roles list from the token's "roles" claim.
     *
     * @param token raw JWT string
     * @return list of role strings (e.g. ["USER", "ARCHITECT"])
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object rolesClaim = parseClaims(token).get(ROLES_CLAIM);
        if (rolesClaim instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    /**
     * Returns true if the token signature is valid and the token has not expired.
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            // Any parsing or signature failure is treated as an invalid token.
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
