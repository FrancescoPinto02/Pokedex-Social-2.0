package com.pokedexsocial.backend.exception;

/**
 * Exception thrown during a user registration request when
 * the e-mail in the request is already used.
 * */
public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
