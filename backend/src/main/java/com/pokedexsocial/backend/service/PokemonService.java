package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.exception.PokemonNotFoundException;
import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.model.TypeEffectiveness;
import com.pokedexsocial.backend.repository.TypeEffectivenessRepository;
import com.pokedexsocial.backend.specification.PokemonSearchCriteria;
import com.pokedexsocial.backend.dto.AbilityDto;
import com.pokedexsocial.backend.dto.AbilityListDto;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.PokemonFiltersDto;
import com.pokedexsocial.backend.dto.PokemonListDto;
import com.pokedexsocial.backend.dto.TypeDto;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.repository.AbilityRepository;
import com.pokedexsocial.backend.repository.PokemonRepository;
import com.pokedexsocial.backend.repository.TypeRepository;
import com.pokedexsocial.backend.specification.PokemonSpecification;

import com.pokedexsocial.backend.util.Range;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing Pokémon.
 * Provides operations to retrieve a single Pokémon by ID
 * and to search Pokémon based on specific criteria.
 */
@Service
public class PokemonService {
    private final PokemonRepository pokemonRepository;
    private final TypeRepository typeRepository;
    private final AbilityRepository abilityRepository;
    private final TypeEffectivenessRepository effectivenessRepository;

    public PokemonService(PokemonRepository pokemonRepository, TypeRepository typeRepository, AbilityRepository abilityRepository, TypeEffectivenessRepository effectivenessRepository) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.abilityRepository = abilityRepository;
        this.effectivenessRepository = effectivenessRepository;
    }

    /**
     * Retrieves a Pokémon by its ID.
     *
     * @param id - the ID of the Pokémon
     * @return the Pokémon DTO
     * @throws PokemonNotFoundException if the Pokémon is not found
     */
    public PokemonDto getPokemonById(Integer id) {
        Pokemon pokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pokemon not found with id " + id));

        PokemonDto dto = new PokemonDto();
        dto.setId(pokemon.getId());
        dto.setNdex(pokemon.getNdex());
        dto.setSpecies(pokemon.getSpecies());
        dto.setForme(pokemon.getForme());
        dto.setDex1(pokemon.getDex1());
        dto.setDex2(pokemon.getDex2());

        dto.setHp(pokemon.getHp());
        dto.setAttack(pokemon.getAttack());
        dto.setDefense(pokemon.getDefense());
        dto.setSpattack(pokemon.getSpattack());
        dto.setSpdefense(pokemon.getSpdefense());
        dto.setSpeed(pokemon.getSpeed());
        dto.setTotal(pokemon.getTotal());

        dto.setWeight(pokemon.getWeight() != null ? pokemon.getWeight().doubleValue() : null);
        dto.setHeight(pokemon.getHeight() != null ? pokemon.getHeight().doubleValue() : null);

        dto.setPokemonClass(pokemon.getPokemonClass());
        dto.setPercentMale(pokemon.getPercentMale() != null ? pokemon.getPercentMale().doubleValue() : null);
        dto.setPercentFemale(pokemon.getPercentFemale() != null ? pokemon.getPercentFemale().doubleValue() : null);

        dto.setEggGroup1(pokemon.getEggGroup1());
        dto.setEggGroup2(pokemon.getEggGroup2());
        dto.setImageUrl(pokemon.getImageUrl());

        // Types
        if (pokemon.getType1() != null) {
            dto.setType1(new TypeDto(pokemon.getType1().getId(), pokemon.getType1().getName()));
        }
        if (pokemon.getType2() != null) {
            dto.setType2(new TypeDto(pokemon.getType2().getId(), pokemon.getType2().getName()));
        }

        // Abilities
        if (pokemon.getAbility1() != null) {
            dto.setAbility1(new AbilityDto(
                    pokemon.getAbility1().getId(),
                    pokemon.getAbility1().getName(),
                    pokemon.getAbility1().getDescription()
            ));
        }
        if (pokemon.getAbility2() != null) {
            dto.setAbility2(new AbilityDto(
                    pokemon.getAbility2().getId(),
                    pokemon.getAbility2().getName(),
                    pokemon.getAbility2().getDescription()
            ));
        }
        if (pokemon.getHiddenAbility() != null) {
            dto.setHiddenAbility(new AbilityDto(
                    pokemon.getHiddenAbility().getId(),
                    pokemon.getHiddenAbility().getName(),
                    pokemon.getHiddenAbility().getDescription()
            ));
        }

        // Calcolo moltiplicatori (come prima)
        Map<String, Double> multipliers = new HashMap<>();
        List<TypeEffectiveness> allAttacks = new ArrayList<>();
        allAttacks.addAll(effectivenessRepository.findByDefenderType(pokemon.getType1()));
        if (pokemon.getType2() != null) {
            allAttacks.addAll(effectivenessRepository.findByDefenderType(pokemon.getType2()));
        }

        for (TypeEffectiveness te : allAttacks) {
            String attackerName = te.getAttackerType().getName();
            multipliers.merge(attackerName, te.getMultiplier().doubleValue(), (oldVal, newVal) -> oldVal * newVal);
        }

        Map<String, Double> weaknesses = multipliers.entrySet().stream()
                .filter(e -> e.getValue() > 1.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Double> resistances = multipliers.entrySet().stream()
                .filter(e -> e.getValue() > 0.0 && e.getValue() < 1.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Double> neutral = multipliers.entrySet().stream()
                .filter(e -> e.getValue() == 1.0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dto.setWeaknesses(weaknesses);
        dto.setResistances(resistances);
        dto.setNeutral(neutral);

        return dto;
    }

    /**
     * Searches Pokémon based on criteria and pagination.
     *
     * @param criteria - the search criteria
     * @param pageable - pagination information
     * @return a page of Pokémon DTOs
     */
    public Page<PokemonListDto> search(PokemonSearchCriteria criteria, Pageable pageable) {
        var spec = PokemonSpecification.fromCriteria(criteria);
        Page<Pokemon> page = pokemonRepository.findAll(spec, pageable);

        return page.map(p -> {
            List<TypeDto> types = new ArrayList<>();
            if (p.getType1() != null) types.add(new TypeDto(p.getType1().getId(), p.getType1().getName()));
            if (p.getType2() != null) types.add(new TypeDto(p.getType2().getId(), p.getType2().getName()));

            return new PokemonListDto(
                    p.getId(),
                    p.getNdex(),
                    p.getSpecies(),
                    p.getForme(),
                    p.getPokemonClass(),
                    types,
                    p.getImageUrl()
            );
        });
    }

    public PokemonFiltersDto getFilters() {
        // Retrieves all types
        List<TypeDto> types = typeRepository.findAll().stream()
                .map(t -> new TypeDto(t.getId(), t.getName()))
                .toList();

        // Retrieves all abilities
        List<AbilityListDto> abilities = abilityRepository.findAll().stream()
                .map(a -> new AbilityListDto(a.getId(), a.getName()))
                .toList();

        // ndex
        Range<Integer> ndexRange = new Range<>(
                pokemonRepository.findMinNdex(),
                pokemonRepository.findMaxNdex()
        );

        // height
        Range<BigDecimal> heightRange = new Range<>(
                pokemonRepository.findMinHeight(),
                pokemonRepository.findMaxHeight()
        );

        // width
        Range<BigDecimal> widthRange = new Range<>(
                pokemonRepository.findMinWeight(),
                pokemonRepository.findMaxWeight()
        );

        return new  PokemonFiltersDto(
                types,
                abilities,
                ndexRange,
                widthRange,
                heightRange
        );
    }
}
