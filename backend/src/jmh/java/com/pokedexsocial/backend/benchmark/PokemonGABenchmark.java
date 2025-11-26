package com.pokedexsocial.backend.benchmark;

import com.pokedexsocial.backend.benchmark.stub.BenchmarkInitializer;
import com.pokedexsocial.backend.benchmark.stub.BenchmarkPokemonSwapMutation;
import com.pokedexsocial.backend.benchmark.stub.PokedexJsonLoader;
import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.fitness.PokemonTeamFitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.metaheuristics.SimpleGeneticAlgorithm;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.PokemonTeamSinglePointCrossover;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.PokemonTeamTwoPointCrossover;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.PokemonTeamUniformCrossover;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.KTournamentSelection;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.RankSelection;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.RouletteWheelSelection;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class PokemonGABenchmark {

    @Param({"roulette", "ktournament", "rank"})
    public String selectionType;

    @Param({"uniform", "single", "two"})
    public String crossoverType;

    private SimpleGeneticAlgorithm<PokemonTeamGA> ga;
    private PokedexJsonLoader loader;

    // Accumulatori
    private double fitnessSum;
    private long timeSum;
    private int generationsSum;
    private int count;

    // Variabile temporanea per raccogliere i risultati della singola invocazione
    private Results<PokemonTeamGA> lastResult;
    private long lastTime;

    @Setup(Level.Trial)
    public void setupTrial() {
        loader = new PokedexJsonLoader();

        fitnessSum = 0;
        generationsSum = 0;
        count = 0;
    }

    @Setup(Level.Invocation)
    public void setupGA() {
        Initializer<PokemonTeamGA> initializer = new BenchmarkInitializer(loader, 100);
        FitnessFunction<PokemonTeamGA> fitness = new PokemonTeamFitnessFunction();

        SelectionOperator<PokemonTeamGA> selection =
                switch (selectionType) {
                    case "roulette" -> new RouletteWheelSelection<>();
                    case "ktournament" -> new KTournamentSelection<>();
                    default -> new RankSelection<>();
                };

        CrossoverOperator<PokemonTeamGA> crossover =
                switch (crossoverType) {
                    case "uniform" -> new PokemonTeamUniformCrossover();
                    case "single" -> new PokemonTeamSinglePointCrossover();
                    default -> new PokemonTeamTwoPointCrossover();
                };

        MutationOperator<PokemonTeamGA> mutation =
                new BenchmarkPokemonSwapMutation(loader, 0.3);

        ga = new SimpleGeneticAlgorithm<>(
                fitness,
                initializer,
                selection,
                crossover,
                mutation,
                1.0,
                40,
                10
        );
    }

    @Benchmark
    public Results<PokemonTeamGA> runGA() throws Exception {
        lastResult = ga.run();
        return lastResult;
    }

    @TearDown(Level.Invocation)
    public void collectStats() {
        double bf = lastResult.getBestIndividual().getFitness();
        int gens = lastResult.getGenerations().size();

        fitnessSum += bf;
        generationsSum += gens;
        count++;
    }

    @TearDown(Level.Trial)
    public void printAverages() {
        System.out.println("\n=== MEDIE PER CONFIGURAZIONE ===");
        System.out.printf("Selection: %s | Crossover: %s%n", selectionType, crossoverType);
        System.out.printf("Media Fitness: %.3f%n", fitnessSum / count);
        System.out.printf("Media Iterazioni: %.2f%n", (double) generationsSum / count);
        System.out.println("================================\n");
    }
}
