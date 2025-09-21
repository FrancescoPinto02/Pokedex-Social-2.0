package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.exception.PokemonNotFoundException;
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
import java.util.List;

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

    /**
     * Constructor.
     *
     * @param pokemonRepository the Pokémon repository
     */
    public PokemonService(PokemonRepository pokemonRepository, TypeRepository typeRepository, AbilityRepository abilityRepository) {
        this.pokemonRepository = pokemonRepository;
        this.typeRepository = typeRepository;
        this.abilityRepository = abilityRepository;
    }

    /**
     * Retrieves a Pokémon by its ID.
     *
     * @param id - the ID of the Pokémon
     * @return the Pokémon DTO
     * @throws PokemonNotFoundException if the Pokémon is not found
     */
    public PokemonDto getPokemonById(Integer id) {
        Pokemon p = pokemonRepository.findById(id)
                .orElseThrow(() -> new PokemonNotFoundException("Pokemon with id " + id + " not found"));

        return new PokemonDto(
                p.getId(),
                p.getNdex(),
                p.getSpecies(),
                p.getForme(),
                p.getDex1(),
                p.getDex2(),
                p.getType1() != null ? new TypeDto(p.getType1().getId(), p.getType1().getName()) : null,
                p.getType2() != null ? new TypeDto(p.getType2().getId(), p.getType2().getName()) : null,
                p.getAbility1() != null ? new AbilityDto(p.getAbility1().getId(), p.getAbility1().getName(), p.getAbility1().getDescription()) : null,
                p.getAbility2() != null ? new AbilityDto(p.getAbility2().getId(), p.getAbility2().getName(), p.getAbility2().getDescription()) : null,
                p.getHiddenAbility() != null ? new AbilityDto(p.getHiddenAbility().getId(), p.getHiddenAbility().getName(), p.getHiddenAbility().getDescription()) : null,
                p.getHp(),
                p.getAttack(),
                p.getDefense(),
                p.getSpattack(),
                p.getSpdefense(),
                p.getSpeed(),
                p.getTotal(),
                p.getWeight(),
                p.getHeight(),
                p.getPokemonClass(),
                p.getPercentMale(),
                p.getPercentFemale(),
                p.getEggGroup1(),
                p.getEggGroup2(),
                p.getImageUrl()
        );
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
