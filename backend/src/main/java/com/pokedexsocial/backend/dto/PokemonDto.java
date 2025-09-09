package com.pokedexsocial.backend.dto;

import java.math.BigDecimal;

/** Data Transfer Object for a Pokémon. */
public record PokemonDto(
        Integer id,
        Integer ndex,
        String species,
        String forme,
        String dex1,
        String dex2,
        TypeDto type1,
        TypeDto type2,
        AbilityDto ability1,
        AbilityDto ability2,
        AbilityDto hiddenAbility,
        int hp,
        int attack,
        int defense,
        int spattack,
        int spdefense,
        int speed,
        int total,
        BigDecimal weight,
        BigDecimal height,
        String pokemonClass,
        BigDecimal percentMale,
        BigDecimal percentFemale,
        String eggGroup1,
        String eggGroup2,
        String imageUrl
) {}
