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
import org.openjdk.jmh.annotations.*;

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

    @Setup(Level.Trial)
    public void loadData() {
        loader = new PokedexJsonLoader();
    }

    @Setup(Level.Invocation)
    public void setupGA() {

        Initializer<PokemonTeamGA> initializer = new BenchmarkInitializer(loader, 200);
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

        long start = System.currentTimeMillis();
        Results<PokemonTeamGA> results = ga.run();
        long end = System.currentTimeMillis();

        // Estrai informazioni utili
        double bestFitness = results.getBestIndividual().getFitness();
        int generations = results.getGenerations().size();
        long time = end - start;

        // Registra il risultato
        ResultsCollector.record(
                new ResultsCollector.Entry(
                        selectionType,
                        crossoverType,
                        bestFitness,
                        generations,
                        time
                )
        );

        return results;
    }

    @TearDown(Level.Trial)
    public void exportResults() {
        System.out.println("=== RISULTATI RACCOLTI PER IL TRIAL ===");

        for (ResultsCollector.Entry e : ResultsCollector.getAll()) {
            System.out.printf(
                    "sel=%s, cross=%s, best=%.3f, gen=%d, time=%d ms%n",
                    e.selectionType, e.crossoverType,
                    e.bestFitness, e.generations, e.timeMillis
            );
        }

        System.out.println("========================================");
    }
}
