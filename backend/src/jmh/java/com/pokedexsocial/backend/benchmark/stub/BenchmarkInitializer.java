package com.pokedexsocial.backend.benchmark.stub;

import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;

public class BenchmarkInitializer extends Initializer<PokemonTeamGA> {

    private final PokedexJsonLoader loader;
    private final int populationSize;

    public BenchmarkInitializer(PokedexJsonLoader loader, int populationSize) {
        this.loader = loader;
        this.populationSize = populationSize;
    }

    @Override
    public Population<PokemonTeamGA> initialize() {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, populationSize);

        for (int i = 0; i < populationSize; i++) {
            PokemonGA[] team = new PokemonGA[PokemonTeamGA.MAX_TEAM_MEMBERS];
            for (int j = 0; j < team.length; j++) {
                team[j] = loader.randomPokemon();
            }
            pop.add(new PokemonTeamGA(team));
        }

        return pop;
    }
}

