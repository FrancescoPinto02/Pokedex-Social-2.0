package com.pokedexsocial.backend.exception;

/**
 * Exception thrown when the login fail due to incorrect email or Password
 * */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
