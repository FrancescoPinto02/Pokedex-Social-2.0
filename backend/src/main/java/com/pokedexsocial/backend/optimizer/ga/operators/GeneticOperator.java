package com.pokedexsocial.backend.optimizer.ga.operators;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;

import java.util.Random;

public abstract class GeneticOperator<T extends Individual> {

    /*@ public normal_behavior
      @   requires population != null;
      @   requires rand != null;
      @
      @   requires (\forall Object o; population.contains(o); o instanceof Individual);
      @   requires (\forall T ind; population.contains(ind); ind.fitness >= 0);
      @
      @   ensures \result != null;
      @   ensures (\forall Object o; \result.contains(o); o instanceof Individual);
      @   ensures (\forall T ind; \result.contains(ind); ind.fitness >= 0);
      @*/

    /*@ also
      @ public exceptional_behavior
      @   requires population != null;
      @   requires rand != null;
      @
      @   requires (\forall Object o; population.contains(o); o instanceof Individual);
      @   requires (\forall T ind; population.contains(ind); ind.fitness >= 0);
      @
      @   signals (CloneNotSupportedException e) true;
      @*/
    public abstract Population<T> apply(Population<T> population, Random rand)
            throws CloneNotSupportedException;
}

