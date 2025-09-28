package com.pokedexsocial.backend.optimizer.ga.initializer;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.team.PokemonTeamGenerator;
import org.springframework.stereotype.Component;

@Component
public class PokemonTeamInitializer extends Initializer<PokemonTeamGA>{
    private final PokemonTeamGenerator pokemonTeamGenerator;

    public PokemonTeamInitializer(PokemonTeamGenerator pokemonTeamGenerator) {
        this.pokemonTeamGenerator = pokemonTeamGenerator;
    }

    public Population<PokemonTeamGA> initialize(int numberOfIndividuals) {
        FixedSizePopulation<PokemonTeamGA> population = new FixedSizePopulation<>(0, numberOfIndividuals);
        for(int i = 0; i < numberOfIndividuals; i++){
            PokemonTeamGA individual = pokemonTeamGenerator.generatePokemonTeam(PokemonTeamGA.MAX_TEAM_MEMBERS);
            population.add(individual);
        }

        return population;
    }

    @Override
    public Population<PokemonTeamGA> initialize() {
        return initialize(100);
    }
}
