package com.pokedexsocial.backend.dto;

import com.pokedexsocial.backend.util.Range;

import java.math.BigDecimal;
import java.util.List;

public record PokemonFiltersDto(
        List<TypeDto> types,
        List<AbilityListDto> abilities,
        Range<Integer> ndexRange,
        Range<BigDecimal> weightRange,
        Range<BigDecimal> heightRange
) {}
