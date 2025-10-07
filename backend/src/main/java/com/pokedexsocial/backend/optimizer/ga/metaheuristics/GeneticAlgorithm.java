package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.results.Results;

/**
 * Classe astratta che definisce la struttura base di un algoritmo genetico.
 * Impone l'esistenza di tutti i componenti principali (fitness, inizializzatore, operatori).
 *
 * @param <T> tipo dell'individuo (es. PokemonTeamGA)
 */
public abstract class GeneticAlgorithm<T extends Individual> {

    //@ spec_public
    private final FitnessFunction<T> fitnessFunction;
    //@ spec_public
    private final Initializer<T> initializer;
    //@ spec_public
    private final SelectionOperator<T> selectionOperator;
    //@ spec_public
    private final CrossoverOperator<T> crossoverOperator;
    //@ spec_public
    private final MutationOperator<T> mutationOperator;
    /*@
      @ public invariant fitnessFunction != null;
      @ public invariant initializer != null;
      @ public invariant selectionOperator != null;
      @ public invariant crossoverOperator != null;
      @ public invariant mutationOperator != null;
      @*/



    /*@
      @ // Precondizione: tutti i parametri devono essere non null
      @ requires fitnessFunction != null;
      @ requires initializer != null;
      @ requires selectionOperator != null;
      @ requires crossoverOperator != null;
      @ requires mutationOperator != null;
      @
      @ // Postcondizioni: tutti i campi interni sono correttamente assegnati
      @ ensures this.fitnessFunction == fitnessFunction;
      @ ensures this.initializer == initializer;
      @ ensures this.selectionOperator == selectionOperator;
      @ ensures this.crossoverOperator == crossoverOperator;
      @ ensures this.mutationOperator == mutationOperator;
      @*/
    public GeneticAlgorithm(FitnessFunction<T> fitnessFunction, Initializer<T> initializer,
                            SelectionOperator<T> selectionOperator, CrossoverOperator<T> crossoverOperator,
                            MutationOperator<T> mutationOperator) {
        this.fitnessFunction = fitnessFunction;
        this.initializer = initializer;
        this.selectionOperator = selectionOperator;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
    }

    /*@
      @ // Effetto: esegue l'algoritmo genetico e restituisce un oggetto Results non null
      @ ensures \result != null;
      @ signals (CloneNotSupportedException e) true;
      @*/
    public abstract Results<T> run() throws CloneNotSupportedException;

    /*@ public normal_behavior
      @ ensures \result == fitnessFunction;
      @ ensures \result != null;
      @*/
    public /*@ pure @*/ FitnessFunction<T> getFitnessFunction() {
        return fitnessFunction;
    }

    /*@ public normal_behavior
      @ ensures \result == initializer;
      @ ensures \result != null;
      @*/
    public /*@ pure @*/ Initializer<T> getInitializer() {
        return initializer;
    }

    /*@ public normal_behavior
      @ ensures \result == selectionOperator;
      @ ensures \result != null;
      @*/
    public /*@ pure @*/ SelectionOperator<T> getSelectionOperator() {
        return selectionOperator;
    }

    /*@ public normal_behavior
      @ ensures \result == crossoverOperator;
      @ ensures \result != null;
      @*/
    public /*@ pure @*/ CrossoverOperator<T> getCrossoverOperator() {
        return crossoverOperator;
    }

    /*@ public normal_behavior
      @ ensures \result == mutationOperator;
      @ ensures \result != null;
      @*/
    public /*@ pure @*/ MutationOperator<T> getMutationOperator() {
        return mutationOperator;
    }
}
