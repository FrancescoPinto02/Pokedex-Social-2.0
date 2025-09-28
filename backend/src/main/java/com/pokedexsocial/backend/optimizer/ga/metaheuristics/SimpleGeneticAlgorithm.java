package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

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

    private static final Logger logger = (Logger) LoggerFactory.getLogger(SimpleGeneticAlgorithm.class);

    private final double mutationProbability; // Probabilità di applicare la mutazione
    private final int maxIterations; // Numero massimo di generazioni
    private final int maxIterationsNoImprovements; // Numero massimo di generazioni senza miglioramenti

    /**
     * Costruttore iniettabile da Spring.
     * Tutti i parametri numerici sono configurabili da application.yml.
     */
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

        // Valida parametri
        if (mutationProbability < 0.0 || mutationProbability > 1.0) {
            logger.warn("⚠️ Valore non valido per mutationProbability ({}). Impostato a 1.0.", mutationProbability);
            this.mutationProbability = 1.0;
        } else {
            this.mutationProbability = mutationProbability;
        }

        this.maxIterations = Math.max(maxIterations, 1);
        this.maxIterationsNoImprovements = Math.max(maxIterationsNoImprovements, 0);

        logger.info("⚙️ SimpleGeneticAlgorithm inizializzato | mutationProbability={} | maxIterations={} | maxIterationsNoImprovements={}",
                this.mutationProbability, this.maxIterations, this.maxIterationsNoImprovements);
    }

    /**
     * Esegue il ciclo evolutivo principale.
     *
     * @return i risultati dell'esecuzione (generazioni, log, best team, ecc.)
     */
    @Override
    public Results<T> run() throws CloneNotSupportedException {
        StopWatch timer = new StopWatch();
        timer.start();

        logger.info("🚀 Avvio algoritmo genetico | maxIterations={} | mutationProbability={}", maxIterations, mutationProbability);

        Random rand = new Random();
        List<String> logEntries = new ArrayList<>();
        Stack<Population<T>> generations = new Stack<>();

        // 1️⃣ Inizializzazione popolazione
        Population<T> firstGeneration = getInitializer().initialize();
        getFitnessFunction().evaluate(firstGeneration);
        generations.push(firstGeneration);

        logEntries.add("Gen 1) AvgFitness=" + firstGeneration.getAverageFitness());
        Population<T> bestGeneration = firstGeneration;

        int iterations = 1;
        int iterationsNoImprovements = 0;
        boolean stopEarly = false;

        // 2️⃣ Ciclo principale
        do {
            StringBuilder logEntry = new StringBuilder("Gen ").append(iterations + 1).append(") ");
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

            double bestAvgFitness = bestGeneration.getAverageFitness();
            double newAvgFitness = newGeneration.getAverageFitness();

            logEntry.append("AvgFitness=").append(newAvgFitness).append(" | BestSoFar=").append(bestAvgFitness);

            // 3️⃣ Controlla miglioramento
            boolean improved =
                    (getFitnessFunction().isMaximum() && newGeneration.compareTo(bestGeneration) > 0)
                            || (!getFitnessFunction().isMaximum() && newGeneration.compareTo(bestGeneration) < 0);

            if (improved) {
                bestGeneration = newGeneration;
                iterationsNoImprovements = 0;
                logEntry.append(" ✅ Improvement");
            } else {
                iterationsNoImprovements++;
                stopEarly = (maxIterationsNoImprovements > 0 && iterationsNoImprovements >= maxIterationsNoImprovements);
                if (stopEarly) {
                    logEntry.append(" ⏹️ Early stop (no improvements for ").append(iterationsNoImprovements).append(" generations)");
                }
            }

            logEntries.add(logEntry.toString());

        } while (iterations < maxIterations && !stopEarly);

        timer.stop();
        logger.info("🏁 GA completato in {} ms | Generazioni totali: {} | Migliore fitness: {}",
                timer.getTotalTimeMillis(), iterations, bestGeneration.getBestIndividual().getFitness());

        return new Results<>(this, generations, bestGeneration, logEntries);
    }

    // Getters
    public double getMutationProbability() {
        return mutationProbability;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public int getMaxIterationsNoImprovements() {
        return maxIterationsNoImprovements;
    }
}
