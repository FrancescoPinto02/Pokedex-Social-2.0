package com.pokedexsocial.backend.optimizer.ga.operators.mutation;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PokemonSwapMutation extends MutationOperator<PokemonTeamGA> {

    private final double mutationProbability;
    private final PokemonGenerator pokemonGenerator;

    public PokemonSwapMutation(
            PokemonGenerator pokemonGenerator,
            @Value("${optimizer.mutation.probability:0.3}") double mutationProbability
    ) {
        this.pokemonGenerator = pokemonGenerator;
        this.mutationProbability = mutationProbability;
    }

    public double getMutationProbability() {
        return mutationProbability;
    }

    @Override
    public Population<PokemonTeamGA> apply(Population<PokemonTeamGA> population, Random rand)
            throws CloneNotSupportedException {

        Population<PokemonTeamGA> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        for (PokemonTeamGA individual : population) {
            if (rand.nextDouble() <= mutationProbability) {
                PokemonTeamGA mutatedIndividual = mutate(individual, rand);
                newPopulation.add(mutatedIndividual);
            } else {
                newPopulation.add(individual);
            }
        }

        return newPopulation;
    }

    private PokemonTeamGA mutate(PokemonTeamGA individual, Random rand) {
        // Clona la codifica per evitare di modificare l'originale
        PokemonGA[] originalCoding = individual.getCoding();
        PokemonGA[] newCoding = originalCoding.clone();

        int position = rand.nextInt(newCoding.length);
        newCoding[position] = pokemonGenerator.generatePokemon();

        return new PokemonTeamGA(newCoding);
    }
}
