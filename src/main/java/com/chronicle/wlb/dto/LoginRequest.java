package com.chronicle.wlb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for the POST /api/auth/login endpoint.
 * Credentials are validated against the Identities table; a JWT is returned on success.
 */
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
