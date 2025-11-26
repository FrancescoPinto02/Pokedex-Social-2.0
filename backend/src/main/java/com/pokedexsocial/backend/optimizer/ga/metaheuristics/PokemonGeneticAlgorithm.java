package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PokemonGeneticAlgorithm extends SimpleGeneticAlgorithm<PokemonTeamGA> {

    public PokemonGeneticAlgorithm(
            @Qualifier("pokemonTeamFitnessFunction") FitnessFunction<PokemonTeamGA> fitnessFunction,
            Initializer<PokemonTeamGA> initializer,
            @Qualifier("RankSelection") SelectionOperator<PokemonTeamGA> selectionOperator,
            @Qualifier("Uniform") CrossoverOperator<PokemonTeamGA> crossoverOperator,
            MutationOperator<PokemonTeamGA> mutationOperator,
            @Value("${optimizer.mutation-probability:1.0}") double mutationProbability,
            @Value("${optimizer.max-iterations:40}") int maxIterations,
            @Value("${optimizer.max-no-improvements:20}") int maxIterationsNoImprovements
    ) {
        super(fitnessFunction, initializer, selectionOperator, crossoverOperator, mutationOperator,
                mutationProbability, maxIterations, maxIterationsNoImprovements);
    }
}
