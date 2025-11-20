package com.pokedexsocial.backend.benchmark;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.results.Results;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResultsCollector {

    public static class Entry {
        public final String selectionType;
        public final String crossoverType;
        public final double bestFitness;
        public final int generations;
        public final long timeMillis;

        public Entry(String selectionType, String crossoverType,
                     double bestFitness, int generations, long timeMillis) {
            this.selectionType = selectionType;
            this.crossoverType = crossoverType;
            this.bestFitness = bestFitness;
            this.generations = generations;
            this.timeMillis = timeMillis;
        }
    }

    private static final ConcurrentLinkedQueue<Entry> data =
            new ConcurrentLinkedQueue<>();

    public static void record(Entry entry) {
        data.add(entry);
    }

    public static List<Entry> getAll() {
        return new ArrayList<>(data);
    }

}

