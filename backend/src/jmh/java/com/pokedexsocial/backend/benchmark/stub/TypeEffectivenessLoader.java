package com.pokedexsocial.backend.benchmark.stub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TypeEffectivenessLoader {

    public Map<PokemonTypeName, Map<PokemonTypeName, Double>> loadEffectiveness() {
        try (InputStream in = getClass().getResourceAsStream("/type-effectiveness.json")) {
            if (in == null) {
                throw new IllegalStateException("type-effectiveness.json non trovato nel classpath");
            }

            ObjectMapper mapper = new ObjectMapper();

            // RAW = Map<String, Map<String, Double>>
            Map<String, Map<String, Double>> raw =
                    mapper.readValue(in, new TypeReference<>() {});

            Map<PokemonTypeName, Map<PokemonTypeName, Double>> result =
                    new HashMap<>();

            raw.forEach((attackerStr, defenders) -> {
                PokemonTypeName attacker = PokemonTypeName.valueOf(attackerStr);

                Map<PokemonTypeName, Double> converted = new HashMap<>();
                defenders.forEach((defStr, mult) ->
                        converted.put(PokemonTypeName.valueOf(defStr), mult));

                result.put(attacker, converted);
            });

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Errore caricando type-effectiveness.json", e);
        }
    }
}

