package com.pokedexsocial.backend.benchmark.stub;

import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;

import java.util.HashMap;
import java.util.Map;

public class BenchmarkPokemonTypePool {

    private final Map<PokemonTypeName, PokemonType> pool = new HashMap<>();

    public BenchmarkPokemonTypePool(
            Map<PokemonTypeName, Map<PokemonTypeName, Double>> offensiveMatrix
    ) {

        // --- Costruisci mappa difensiva (invertendo l’offensiva)
        Map<PokemonTypeName, Map<PokemonTypeName, Double>> defensiveMatrix =
                new HashMap<>();

        for (var attacker : offensiveMatrix.keySet()) {
            for (var defender : offensiveMatrix.get(attacker).keySet()) {

                defensiveMatrix
                        .computeIfAbsent(defender, k -> new HashMap<>())
                        .put(attacker, offensiveMatrix.get(attacker).get(defender));
            }
        }

        // --- Crea tutti i PokemonType completi
        for (PokemonTypeName t : PokemonTypeName.values()) {

            Map<PokemonTypeName, Double> off =
                    offensiveMatrix.getOrDefault(t, new HashMap<>());

            Map<PokemonTypeName, Double> def =
                    defensiveMatrix.getOrDefault(t, new HashMap<>());

            pool.put(t, new PokemonType(t, off, def));
        }
    }

    public PokemonType get(PokemonTypeName name) {
        return pool.get(name);
    }
}

