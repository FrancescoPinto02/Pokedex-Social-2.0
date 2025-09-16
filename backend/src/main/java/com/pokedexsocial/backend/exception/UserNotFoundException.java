package com.pokedexsocial.backend.exception;

/**
 * Exception thrown when a user entity is not found.
 * */
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
