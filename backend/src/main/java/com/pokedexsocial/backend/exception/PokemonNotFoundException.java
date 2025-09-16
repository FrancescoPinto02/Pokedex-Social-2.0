package com.pokedexsocial.backend.exception;

public class PokemonNotFoundException extends NotFoundException {
    public PokemonNotFoundException(String message) {
        super(message);
    }
}
