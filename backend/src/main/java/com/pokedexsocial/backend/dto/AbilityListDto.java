package com.pokedexsocial.backend.dto;

/**
 * Data Transfer Object representing a Pokémon ability in a list or filter context.
 * <p>
 * This DTO only contains the minimal fields
 * required for listing or filtering abilities: {@code id} and {@code name}.
 * It does not include the ability description to optimize performance.
 * </p>
 *
 * @param id   the unique identifier of the ability
 * @param name the name of the ability
 */
public record AbilityListDto(Integer id, String name) {
}
