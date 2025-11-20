package com.pokedexsocial.backend.benchmark.stub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;

import java.io.InputStream;
import java.util.List;

import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;


import java.util.ArrayList;

import java.util.Map;
import java.util.Random;

public class PokedexJsonLoader {

    private final List<PokemonGA> pokemons;
    private final Random rand = new Random(123);
    private final BenchmarkPokemonTypePool typePool;

    public PokedexJsonLoader() {

        // 1. Carica matrice efficacia tipi
        Map<PokemonTypeName, Map<PokemonTypeName, Double>> eff =
                new TypeEffectivenessLoader().loadEffectiveness();

        // 2. Crea TypePool statico
        this.typePool = new BenchmarkPokemonTypePool(eff);

        // 3. Carica Pokémon
        this.pokemons = load();
    }

    private List<PokemonGA> load() {
        try (InputStream in = getClass().getResourceAsStream("/pokedex-export.json")) {
            if (in == null) {
                throw new IllegalStateException("pokedex-export.json NON trovato nel classpath");
            }

            ObjectMapper mapper = new ObjectMapper();
            List<PokemonExportDTO> dtos = mapper.readValue(
                    in, new TypeReference<>() {}
            );

            List<PokemonGA> result = new ArrayList<>();
            for (PokemonExportDTO dto : dtos) {

                PokemonType type1 = typePool.get(dto.type1);
                PokemonType type2 = typePool.get(dto.type2);

                PokemonGA ga = new PokemonGA(
                        dto.number,
                        dto.name,
                        type1,
                        type2,
                        dto.hp,
                        dto.attack,
                        dto.defense,
                        dto.specialAttack,
                        dto.specialDefense,
                        dto.speed,
                        dto.rarity
                );

                // Resistenze / debolezze prese dal JSON (ok)
                ga.setResistances(dto.resistances);
                ga.setWeaknesses(dto.weaknesses);

                result.add(ga);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Errore caricando pokedex-export.json", e);
        }
    }

    public PokemonGA randomPokemon() {
        return pokemons.get(rand.nextInt(pokemons.size()));
    }

    public List<PokemonGA> getAll() {
        return pokemons;
    }
}