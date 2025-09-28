package com.pokedexsocial.backend.optimizer.pokemon.team;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PokemonTeamGenerator {
    private final PokemonGenerator pokemonGenerator;

    public PokemonTeamGenerator(PokemonGenerator pokemonGenerator) {
        this.pokemonGenerator = pokemonGenerator;
    }

    public PokemonTeamGA generatePokemonTeam(int size) {
        List<PokemonGA> team = new ArrayList<>();
        for(int i=0; i<size; i++){
            team.add(pokemonGenerator.generatePokemon());
        }
        return new PokemonTeamGA(team.toArray(new PokemonGA[0]));
    }
}
