package com.pokedexsocial.backend.benchmark;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;

public class BenchmarkRunner {
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PokemonGABenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MILLISECONDS)

                .warmupIterations(5)
                .warmupTime(TimeValue.milliseconds(200))

                .measurementIterations(10)
                .measurementTime(TimeValue.milliseconds(300))

                .forks(3)

                .threads(1)
                .shouldDoGC(true)

                .build();

        new Runner(opt).run();
    }
}