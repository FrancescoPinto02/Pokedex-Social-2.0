package com.pokedexsocial.backend.security;

/**
 * Represents an authenticated user extracted from a JWT token.
 *
 * <p>This record is used to store basic user information such as ID, username, and role
 * during the processing of a request.</p>
 *
 * @param id       the unique identifier of the user
 * @param username the username of the authenticated user
 * @param role     the role of the user (e.g., "USER", "ADMIN")
 */
public record AuthenticatedUser(
        Integer id,
        String username,
        String role
) {}
