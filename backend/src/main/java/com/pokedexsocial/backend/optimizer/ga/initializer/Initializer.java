package com.pokedexsocial.backend.optimizer.ga.initializer;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;

public abstract class Initializer<T extends Individual> {

    public abstract Population<T> initialize();

}
