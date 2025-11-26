package com.pokedexsocial.backend.benchmark;

import java.util.ArrayList;
import java.util.List;

public class BenchmarkStats {

    public static class Entry {
        public final String selection;
        public final String crossover;

        public final int generations;
        public final double bestFitness;

        public Entry(String selection, String crossover,
                     int generations, double bestFitness) {

            this.selection = selection;
            this.crossover = crossover;
            this.generations = generations;
            this.bestFitness = bestFitness;
        }
    }

    private static final List<Entry> results = new ArrayList<>();

    public static void record(Entry e) {
        results.add(e);
    }

    public static List<Entry> getResults() {
        return results;
    }

    public static void clear() {
        results.clear();
    }
}
