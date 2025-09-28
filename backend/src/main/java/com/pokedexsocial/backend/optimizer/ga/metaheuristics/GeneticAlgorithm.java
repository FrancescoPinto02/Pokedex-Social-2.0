package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.results.Results;

public abstract class GeneticAlgorithm<T extends Individual> {

    private final FitnessFunction<T> fitnessFunction;
    private final Initializer<T> initializer;
    private final SelectionOperator<T> selectionOperator;
    private final CrossoverOperator<T> crossoverOperator;
    private final MutationOperator<T> mutationOperator;

    public GeneticAlgorithm(FitnessFunction<T> fitnessFunction, Initializer<T> initializer,
                            SelectionOperator<T> selectionOperator, CrossoverOperator<T> crossoverOperator,
                            MutationOperator<T> mutationOperator) {
        this.fitnessFunction = fitnessFunction;
        this.initializer = initializer;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
    }

    public abstract Results<T> run() throws CloneNotSupportedException;

    public FitnessFunction<T> getFitnessFunction() {
        return fitnessFunction;
    }

    public Initializer<T> getInitializer() {
        return initializer;
    }

    public SelectionOperator<T> getSelectionOperator() {
        return selectionOperator;
    }

    public CrossoverOperator<T> getCrossoverOperator() {
        return crossoverOperator;
    }

    public MutationOperator<T> getMutationOperator() {
        return mutationOperator;
    }
}
