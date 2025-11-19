package com.pokedexsocial.backend.benchmark;

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
import com.pokedexsocial.backend.optimizer.ga.operators.selection.RouletteWheelSelection;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class PokemonGABenchmark {

    @Param({"roulette", "ktournament"})
    public String selectionType;

    @Param({"uniform", "single", "two"})
    public String crossoverType;

    private SimpleGeneticAlgorithm<PokemonTeamGA> ga;

    @Setup(Level.Trial)
    public void setup() {
        // Loader dai dati JSON reali
        PokedexJsonLoader loader = new PokedexJsonLoader();

        // Initializer: popolazione iniziale
        Initializer<PokemonTeamGA> initializer = new BenchmarkInitializer(loader, 200);

        // Fitness
        FitnessFunction<PokemonTeamGA> fitness = new PokemonTeamFitnessFunction();

        // Selection
        SelectionOperator<PokemonTeamGA> selection =
                selectionType.equals("roulette")
                        ? new RouletteWheelSelection<>()
                        : new KTournamentSelection<>();

        // Crossover
        CrossoverOperator<PokemonTeamGA> crossover = switch (crossoverType) {
            case "uniform" -> new PokemonTeamUniformCrossover();
            case "single" -> new PokemonTeamSinglePointCrossover();
            default -> new PokemonTeamTwoPointCrossover();
        };

        // Mutation
        MutationOperator<PokemonTeamGA> mutation =
                new BenchmarkPokemonSwapMutation(loader, 0.3);

        ga = new SimpleGeneticAlgorithm<>(
                fitness,
                initializer,
                selection,
                crossover,
                mutation,
                1.0,   // probabilità che si applichi l'operatore di mutazione
                40,    // max iterazioni
                10     // max iterazioni senza miglioramenti
        );
    }

    @Benchmark
    public Results<PokemonTeamGA> runGA() throws Exception {
        return ga.run();
    }
}