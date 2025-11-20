package com.pokedexsocial.backend.benchmark.stub;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;

import java.util.Random;

public class BenchmarkPokemonSwapMutation extends MutationOperator<PokemonTeamGA> {

    private final PokedexJsonLoader loader;
    private final double mutationProbability;

    public BenchmarkPokemonSwapMutation(PokedexJsonLoader loader, double mutationProbability) {
        this.loader = loader;
        this.mutationProbability = mutationProbability;
    }

    @Override
    public Population<PokemonTeamGA> apply(Population<PokemonTeamGA> population, Random rand) {
        Population<PokemonTeamGA> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        for (PokemonTeamGA individual : population) {
            if (rand.nextDouble() <= mutationProbability) {
                newPopulation.add(mutate(individual, rand));
            } else {
                newPopulation.add(individual); // qui puoi anche clonare se vuoi totale isolamento
            }
        }
        return newPopulation;
    }

    private PokemonTeamGA mutate(PokemonTeamGA individual, Random rand) {
        PokemonGA[] original = individual.getCoding();
        PokemonGA[] mutated = original.clone();

        int pos = rand.nextInt(mutated.length);
        mutated[pos] = loader.randomPokemon();

        return new PokemonTeamGA(mutated);
    }
}
