package com.pokedexsocial.backend.optimizer.ga.fitness;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;

import java.util.Collections;

// The genetic T is the type of Individuals on which it could be applied
public abstract class FitnessFunction<T extends Individual> {

    //isMaximum = True -> higher fitness is better
    //isMaximum = False -> lower fitness is better
    private final boolean isMaximum;

    public FitnessFunction(boolean isMaximum) {
        this.isMaximum = isMaximum;
    }

    public void evaluate(Population<T> population) {
        for (T individual : population) {
            evaluate(individual);
        }

        T bestIndividual;
        if (isMaximum) {
            bestIndividual = Collections.max(population);
        } else {
            bestIndividual = Collections.min(population);
        }
        population.setBestIndividual(bestIndividual);
    }

    public abstract void evaluate(T individual);

    public boolean isMaximum() {
        return isMaximum;
    }
}
