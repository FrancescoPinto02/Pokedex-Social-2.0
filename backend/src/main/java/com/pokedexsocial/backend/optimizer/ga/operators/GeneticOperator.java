package com.pokedexsocial.backend.optimizer.ga.operators;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;

import java.util.Random;

public abstract class GeneticOperator<T extends Individual> {

    public abstract Population<T> apply(Population<T> population, Random rand) throws CloneNotSupportedException;

}
