package com.caresphere.auth.dto;

/**
 * Authentication response carrying the JWT and the caller's identity.
 *
 * @param token      signed JWT to send as {@code Authorization: Bearer <token>}
 * @param email      authenticated user's email
 * @param role       authenticated user's role
 * @param expiresInMs token lifetime in milliseconds
 */
public record AuthResponse(String token, String email, String role, long expiresInMs) {
}
