package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.ga.results.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Implementazione di un algoritmo genetico semplice (GA classico).
 * Esegue selezione, crossover e mutazione fino al raggiungimento
 * di un numero massimo di iterazioni o finché non ci sono più miglioramenti.
 *
 * @param <T> tipo dell'individuo (es. PokemonTeamGA)
 */
public class SimpleGeneticAlgorithm<T extends Individual> extends GeneticAlgorithm<T> {
    //@ spec_public
    private final double mutationProbability;
    //@ spec_public
    private final int maxIterations;
    //@ spec_public
    private final int maxIterationsNoImprovements;

    /*@
      @ public invariant 0.0 <= mutationProbability && mutationProbability <= 1.0;
      @ public invariant maxIterations >= 1;
      @ public invariant maxIterationsNoImprovements >= 0;
      @*/

    /*@
      @ requires fitnessFunction != null;
      @ requires initializer != null;
      @ requires selectionOperator != null;
      @ requires crossoverOperator != null;
      @ requires mutationOperator != null;
      @ requires mutationProbability >= 0.0 && mutationProbability <= 1.0;
      @ requires maxIterations >= 1;
      @ requires maxIterationsNoImprovements >= 0;
      @
      @ ensures this.getFitnessFunction() == fitnessFunction;
      @ ensures this.getInitializer() == initializer;
      @ ensures this.getSelectionOperator() == selectionOperator;
      @ ensures this.getCrossoverOperator() == crossoverOperator;
      @ ensures this.getMutationOperator() == mutationOperator;
      @ ensures this.mutationProbability >= 0.0 && this.mutationProbability <= 1.0;
      @ ensures this.maxIterations >= 1;
      @ ensures this.maxIterationsNoImprovements >= 0;
      @*/
    public SimpleGeneticAlgorithm(
            FitnessFunction<T> fitnessFunction,
            Initializer<T> initializer,
            SelectionOperator<T> selectionOperator,
            CrossoverOperator<T> crossoverOperator,
            MutationOperator<T> mutationOperator,
            double mutationProbability,
            int maxIterations,
            int maxIterationsNoImprovements
    ) {
        super(fitnessFunction, initializer, selectionOperator, crossoverOperator, mutationOperator);

        // Validazione parametri con fallback
        if (mutationProbability < 0.0 || mutationProbability > 1.0) {
            this.mutationProbability = 1.0;
        } else {
            this.mutationProbability = mutationProbability;
        }

        this.maxIterations = Math.max(maxIterations, 1);
        this.maxIterationsNoImprovements = Math.max(maxIterationsNoImprovements, 0);
    }

    /*@ also
      @ public normal_behavior
      @ requires getInitializer() != null && getFitnessFunction() != null;
      @ requires getSelectionOperator() != null && getCrossoverOperator() != null && getMutationOperator() != null;
      @
      @ // il risultato dell'algoritmo è sempre valido
      @ ensures \result != null;
      @ // la generazione migliore è sempre presente
      @ ensures \result.getBestGeneration() != null;
      @ // esiste sempre un individuo migliore
      @ ensures \result.getBestGeneration().getBestIndividual() != null;
      @
      @ also
      @ signals (CloneNotSupportedException e) true;
      @*/
    @Override
    public Results<T> run() throws CloneNotSupportedException {

        Random rand = new Random();
        List<String> logEntries = new ArrayList<>();
        Stack<Population<T>> generations = new Stack<>();

        // 1️⃣ Inizializzazione popolazione
        Population<T> firstGeneration = getInitializer().initialize();
        getFitnessFunction().evaluate(firstGeneration);
        generations.push(firstGeneration);

        Population<T> bestGeneration = firstGeneration;

        int iterations = 1;
        int iterationsNoImprovements = 0;
        boolean stopEarly = false;

        // 2️⃣ Ciclo principale
        /*@
          @ loop_invariant generations != null;
          @ loop_invariant !generations.isEmpty();
          @ loop_invariant bestGeneration != null;
          @ loop_invariant iterations >= 1 && iterations <= maxIterations;
          @ loop_invariant iterationsNoImprovements >= 0 && iterationsNoImprovements <= iterations;
          @ loop_invariant (\forall int i; 0 <= i < generations.size(); generations.get(i) != null);
          @ decreasing maxIterations - iterations;
          @*/
        do {
            Population<T> currentGeneration = generations.peek();

            // Selezione
            Population<T> matingPool = getSelectionOperator().apply(currentGeneration, rand);

            // Crossover
            Population<T> offsprings = getCrossoverOperator().apply(matingPool, rand);

            // Mutazione con probabilità
            Population<T> newGeneration = (rand.nextDouble() <= mutationProbability)
                    ? getMutationOperator().apply(offsprings, rand)
                    : offsprings;

            // Valutazione fitness
            getFitnessFunction().evaluate(newGeneration);
            generations.push(newGeneration);
            iterations++;

            // Controllo miglioramento fitness
            boolean improved =
                    (getFitnessFunction().isMaximum() && newGeneration.compareTo(bestGeneration) > 0)
                            || (!getFitnessFunction().isMaximum() && newGeneration.compareTo(bestGeneration) < 0);

            if (improved) {
                bestGeneration = newGeneration;
                iterationsNoImprovements = 0;
            } else {
                iterationsNoImprovements++;
                stopEarly = (maxIterationsNoImprovements > 0 && iterationsNoImprovements >= maxIterationsNoImprovements);
            }

        } while (iterations < maxIterations && !stopEarly);

        return new Results<>(this, generations, bestGeneration, logEntries);
    }

    /*@ public normal_behavior
      @ ensures 0.0 <= \result && \result <= 1.0;
      @*/
    public /*@ pure @*/ double getMutationProbability() {
        return mutationProbability;
    }

    /*@ public normal_behavior
      @ ensures \result >= 1;
      @*/
    public /*@ pure @*/ int getMaxIterations() {
        return maxIterations;
    }

    /*@ public normal_behavior
      @ ensures \result >= 0;
      @*/
    public /*@ pure @*/ int getMaxIterationsNoImprovements() {
        return maxIterationsNoImprovements;
    }
}
