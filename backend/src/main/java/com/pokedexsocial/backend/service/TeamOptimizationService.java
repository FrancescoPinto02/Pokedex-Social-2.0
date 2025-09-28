package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.OptimizationResultDTO;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.metaheuristics.PokemonGeneticAlgorithm;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.springframework.stereotype.Service;

@Service
public class TeamOptimizationService {

    private final PokemonGeneticAlgorithm pokemonGeneticAlgorithm;

    public TeamOptimizationService(PokemonGeneticAlgorithm pokemonGeneticAlgorithm) {
        this.pokemonGeneticAlgorithm = pokemonGeneticAlgorithm;
    }

    public OptimizationResultDTO optimize() throws CloneNotSupportedException {
        Results<PokemonTeamGA> results = pokemonGeneticAlgorithm.run();

        PokemonTeamGA bestTeam = results.getBestIndividual();
        double bestFitness = bestTeam.getFitness();
        int iterations = results.getNumberOfIterations();
        var log = results.getLog();

        return new OptimizationResultDTO(bestTeam, bestFitness, iterations, log);
    }
}
