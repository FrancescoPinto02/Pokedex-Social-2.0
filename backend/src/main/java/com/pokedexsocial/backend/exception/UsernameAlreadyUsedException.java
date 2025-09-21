package com.pokedexsocial.backend.exception;

/**
 * Exception thrown during a user registration request when
 * the username in the request is already used.
 * */
public class UsernameAlreadyUsedException extends RuntimeException {
    public UsernameAlreadyUsedException(String message) {
        super(message);
    }
}
