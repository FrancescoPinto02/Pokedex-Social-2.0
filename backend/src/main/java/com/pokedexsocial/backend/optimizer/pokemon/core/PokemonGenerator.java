package com.pokedexsocial.backend.optimizer.pokemon.core;

import com.pokedexsocial.backend.optimizer.pokemon.pokedex.Pokedex;

import org.springframework.stereotype.Component;


@Component
public class PokemonGenerator {
    private final Pokedex pokedex;

    public PokemonGenerator(Pokedex pokedex) {
        this.pokedex = pokedex;
    }

    public PokemonGA generatePokemon() {
        return pokedex.getRandomPokemon();
    }
}
