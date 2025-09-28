package com.pokedexsocial.backend.optimizer.pokemon.type;

import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.model.TypeEffectiveness;
import com.pokedexsocial.backend.repository.TypeEffectivenessRepository;
import com.pokedexsocial.backend.repository.TypeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PokemonTypePool {
    private Set<PokemonType> types;
    private final TypeRepository typeRepository;
    private final TypeEffectivenessRepository typeEffectivenessRepository;

    public PokemonTypePool(TypeRepository typeRepository,
                           TypeEffectivenessRepository typeEffectivenessRepository) {
        this.typeRepository = typeRepository;
        this.typeEffectivenessRepository = typeEffectivenessRepository;
    }

    @PostConstruct
    public void init() {
        System.out.println("Inizializzo il PokemonTypePool...");
        types = new HashSet<>();
        initialize();
    }

    private void initialize() {
        // Recupera tutti i tipi dal DB
        List<Type> allTypes = typeRepository.findAll();

        // Recupera tutte le relazioni di efficacia
        List<TypeEffectiveness> allEffectiveness = typeEffectivenessRepository.findAll();

        // Crea una mappa rapida Type.id → PokemonTypeName
        Map<Integer, PokemonTypeName> idToName = allTypes.stream()
                .collect(Collectors.toMap(Type::getId, t -> PokemonTypeName.valueOf(t.getName().toUpperCase())));

        // Prepara strutture temporanee per costruire le mappe offensive/difensive
        Map<PokemonTypeName, Map<PokemonTypeName, Double>> offensiveMap = new HashMap<>();
        Map<PokemonTypeName, Map<PokemonTypeName, Double>> defensiveMap = new HashMap<>();

        for (TypeEffectiveness te : allEffectiveness) {
            PokemonTypeName attacker = idToName.get(te.getAttackerType().getId());
            PokemonTypeName defender = idToName.get(te.getDefenderType().getId());
            Double multiplier = te.getMultiplier().doubleValue();

            // Mappa offensiva (attaccante → difensore)
            offensiveMap
                    .computeIfAbsent(attacker, k -> new HashMap<>())
                    .put(defender, multiplier);

            // Mappa difensiva (difensore ← attaccante)
            defensiveMap
                    .computeIfAbsent(defender, k -> new HashMap<>())
                    .put(attacker, multiplier);
        }

        // Crea e aggiungi tutti i PokemonType al Set
        for (PokemonTypeName typeName : idToName.values()) {
            Map<PokemonTypeName, Double> offensiveProps = offensiveMap.getOrDefault(typeName, new HashMap<>());
            Map<PokemonTypeName, Double> defensiveProps = defensiveMap.getOrDefault(typeName, new HashMap<>());

            types.add(new PokemonType(typeName, offensiveProps, defensiveProps));
        }
    }

    public Set<PokemonType> getTypes() {
        return types;
    }

    public Optional<PokemonType> getTypeByName(PokemonTypeName name) {
        return types.stream().filter(t -> t.getName() == name).findFirst();
    }
}
