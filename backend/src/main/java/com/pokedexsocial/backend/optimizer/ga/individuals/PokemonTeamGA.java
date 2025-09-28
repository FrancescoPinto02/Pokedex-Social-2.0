package com.pokedexsocial.backend.optimizer.ga.individuals;

import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;

import java.util.Arrays;

public class PokemonTeamGA extends PokemonArrayIndividual {
    public static final int MAX_TEAM_MEMBERS = 6;


    public PokemonTeamGA(PokemonGA[] coding) {
        super(coding);
    }

    @Override
    public String toString() {
        return "PokemonTeam=" + Arrays.toString(coding) + " Fitness=" + fitness;
    }
}
