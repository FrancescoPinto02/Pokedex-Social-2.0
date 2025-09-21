package com.pokedexsocial.backend.security;

/**
 * Holds the currently authenticated user for the duration of a request.
 *
 * <p>Uses a {@link ThreadLocal} to store the authenticated user information,
 * allowing service and controller layers to access the user without passing it explicitly.
 * Always call {@link #clear()} after request processing to avoid memory leaks.</p>
 */
public class AuthContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT_USER = new ThreadLocal<>();

    /** Sets the authenticated user for the current request. */
    public static void setCurrentUser(AuthenticatedUser user) {
        CURRENT_USER.set(user);
    }

    /** Retrieves the authenticated user for the current request. */
    public static AuthenticatedUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    /** Clears the stored authenticated user after request completion. */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
