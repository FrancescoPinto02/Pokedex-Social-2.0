package com.pokedexsocial.backend.optimizer.ga.individuals;

import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;

public class PokemonArrayIndividual extends EncodedIndividual<PokemonGA[]>{
    public PokemonArrayIndividual(PokemonGA[] coding) {
        super(coding);
    }
}
