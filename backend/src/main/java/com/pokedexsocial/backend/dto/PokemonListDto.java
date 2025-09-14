package com.pokedexsocial.backend.dto;

import java.util.List;

/**
 * Lightweight DTO for listing Pokémon.
 *
 * @param id       the Pokémon ID
 * @param ndex     the National Dex number
 * @param species  the Pokémon species name
 * @param forme    the specific form (if any)
 * @param pokemonClass    the Pokémon class
 * @param types    the Pokémon types (up to 2)
 * @param imageUrl the image URL of the Pokémon
 */
public record PokemonListDto(
        Integer id,
        Integer ndex,
        String species,
        String forme,
        String pokemonClass,
        List<TypeDto> types,
        String imageUrl
) {}
