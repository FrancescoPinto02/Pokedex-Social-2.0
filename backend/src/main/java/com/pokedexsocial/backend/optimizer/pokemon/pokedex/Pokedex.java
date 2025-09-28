package com.pokedexsocial.backend.optimizer.pokemon.pokedex;

import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypePool;
import com.pokedexsocial.backend.repository.PokemonRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Component
public class Pokedex {
    private final PokemonTypePool pokemonTypePool;
    private final PokemonRepository pokemonRepository;
    private final Random random = new Random();

    private HashMap<Integer, List<PokemonGA>> pokemons;
    private int maxNumber;

    private static final Set<Integer> LEGENDARY_NDEX = Set.of(
            150, 249, 250, 382, 383, 384, 483, 484, 487,
            643, 644, 646, 716, 717, 718, 789, 790, 791, 792, 800
    );

    private static final Set<Integer> SUB_LEGENDARY_NDEX = Set.of(
            144, 145, 146, 243, 244, 245, 377, 378, 379, 380, 381,
            480, 481, 482, 485, 486, 488, 638, 639, 640, 641, 642, 645,
            772, 773, 785, 786, 787, 788
    );

    private static final Set<Integer> PSEUDO_LEGENDARY_NDEX = Set.of(
            149, 248, 373, 376, 445, 635, 706, 784
    );

    private static final Set<Integer> MYTHICAL_NDEX = Set.of(
            151, 251, 385, 386, 489, 490, 491, 492, 493, 494, 647, 648,
            649, 719, 720, 721, 801, 802, 807, 808, 809
    );


    public Pokedex(PokemonTypePool pokemonTypePool, PokemonRepository pokemonRepository) {
        this.pokemonTypePool = pokemonTypePool;
        this.pokemonRepository = pokemonRepository;
    }

    /**
     * Carica tutti i Pokémon dal DB all'avvio dell'applicazione
     * e li converte in PokemonGA pronti all'uso.
     */
    @PostConstruct
    public void init() {
        pokemons = new HashMap<>();

        List<Pokemon> allPokemons = pokemonRepository.findAllWithTypes();
        maxNumber = allPokemons.size();

        for (Pokemon dbPokemon : allPokemons) {
            PokemonGA pokemonGA = convertToPokemonGA(dbPokemon);
            pokemons.computeIfAbsent(dbPokemon.getNdex(), k -> new ArrayList<>()).add(pokemonGA);
        }

        System.out.printf("✅ Pokedex caricato con %d Pokémon (fino a N° %d)%n", maxNumber, pokemons.size());
    }

    /**
     * Converte un'entità del DB in un PokemonGA
     */
    private PokemonGA convertToPokemonGA(Pokemon dbPokemon) {
        PokemonType type1 = convertType(dbPokemon.getType1());
        PokemonType type2 = convertType(dbPokemon.getType2());
        PokemonRarity rarity = determineRarity(dbPokemon);

        return new PokemonGA(
                dbPokemon.getNdex(),
                dbPokemon.getSpecies(),
                type1,
                type2,
                dbPokemon.getHp(),
                dbPokemon.getAttack(),
                dbPokemon.getDefense(),
                dbPokemon.getSpattack(),
                dbPokemon.getSpdefense(),
                dbPokemon.getSpeed(),
                rarity
        );
    }

    /**
     * Converte un Type del DB in un PokemonType del pool.
     */
    private PokemonType convertType(Type dbType) {
        if (dbType == null) {
            return new PokemonType(PokemonTypeName.UNDEFINED);
        }

        PokemonTypeName name = PokemonTypeName.valueOf(dbType.getName().toUpperCase());
        return pokemonTypePool.getTypeByName(name)
                .orElse(new PokemonType(PokemonTypeName.UNDEFINED));
    }

    /**
     * Determina la rarità del Pokémon in base al National Dex.
     */
    private PokemonRarity determineRarity(Pokemon dbPokemon) {
        int ndex = dbPokemon.getNdex();
        if (LEGENDARY_NDEX.contains(ndex)) return PokemonRarity.LEGENDARY;
        if (SUB_LEGENDARY_NDEX.contains(ndex)) return PokemonRarity.SUB_LEGENDARY;
        if (PSEUDO_LEGENDARY_NDEX.contains(ndex)) return PokemonRarity.PSEUDO_LEGENDARY;
        if (MYTHICAL_NDEX.contains(ndex)) return PokemonRarity.MYTHICAL;
        return PokemonRarity.COMMON;
    }

    /**
     * Restituisce un Pokémon casuale dal Pokedex (versione già convertita)
     */
    public PokemonGA getRandomPokemon() {
        // Estrai un ndex casuale
        List<Integer> ndexList = new ArrayList<>(pokemons.keySet());
        int randomNdex = ndexList.get(random.nextInt(ndexList.size()));

        // Estrai una forma casuale (se più Pokémon condividono lo stesso ndex)
        List<PokemonGA> forms = pokemons.get(randomNdex);
        return forms.get(random.nextInt(forms.size()));
    }

    /**
     * Restituisce tutti i Pokémon caricati
     */
    public Collection<PokemonGA> getAllPokemons() {
        return pokemons.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    /**
     * Restituisce un Pokémon specifico per ndex
     */
    public Optional<PokemonGA> getByNdex(int ndex) {
        List<PokemonGA> forms = pokemons.get(ndex);
        if (forms == null || forms.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(forms.get(0));
    }
}
