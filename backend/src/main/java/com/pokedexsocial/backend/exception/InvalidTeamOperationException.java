package com.pokedexsocial.backend.exception;

public class InvalidTeamOperationException extends RuntimeException {
    public InvalidTeamOperationException(String message) {
        super(message);
    }
}
