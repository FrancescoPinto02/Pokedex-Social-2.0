package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.exception.NotFoundException;
import com.pokedexsocial.backend.model.Ability;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.repository.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PokemonService}.
 */
public class PokemonServiceTest {
    private PokemonRepository pokemonRepository;
    private PokemonService pokemonService;

    @BeforeEach
    void setUp() {
        pokemonRepository = Mockito.mock(PokemonRepository.class);
        pokemonService = new PokemonService(pokemonRepository);
    }

    /**
     * Test Case: successful retrieval of a complete Pokémon with
     * all type and ability fields populated.
     */
    @Test
    void testGetPokemonById_Success_CompletePokemon() {
        Pokemon pokemon = new Pokemon();
        pokemon.setId(15);
        pokemon.setNdex(15);
        pokemon.setSpecies("Beedrill");
        pokemon.setType1(new Type(12, "bug"));
        pokemon.setType2(new Type(8, "Poison"));
        pokemon.setAbility1(new Ability(68, "Swarm", "Powers up Bug-type moves when the Pokémon’s HP is low."));
        pokemon.setAbility2(new Ability(68, "Swarm", "Powers up Bug-type moves when the Pokémon’s HP is low."));
        pokemon.setHiddenAbility(new Ability(97, "Sniper", "Powers up moves if they become critical hits when attacking."));

        when(pokemonRepository.findById(1)).thenReturn(Optional.of(pokemon));

        // Act
        PokemonDto dto = pokemonService.getPokemonById(1);

        // Assert
        assertNotNull(dto);
        assertEquals(15, dto.id());
        assertEquals("Beedrill", dto.species());
        assertNotNull(dto.type1());
        assertEquals("bug", dto.type1().name());
        assertNotNull(dto.type2());
        assertEquals("Poison", dto.type2().name());
        assertNotNull(dto.ability1());
        assertEquals("Swarm", dto.ability1().name());
        assertNotNull(dto.ability2());
        assertEquals("Swarm", dto.ability2().name());
        assertNotNull(dto.hiddenAbility());
        assertEquals("Sniper", dto.hiddenAbility().name());
    }


    /**
     * Tests Case: successful retrieval of a Pokémon with some
     * optional type and ability fields set to null.
     */
    @Test
    void testGetPokemonById_partialPokemon() {
        // Arrange: Pokémon con campi opzionali null
        Pokemon pokemon = new Pokemon();
        pokemon.setId(1);
        pokemon.setNdex(1);
        pokemon.setSpecies("Bulbasaur");
        pokemon.setType1(new Type(1, "Grass"));
        pokemon.setType2(null);  // type2 assente
        pokemon.setAbility1(new Ability(1, "Overgrow", "Boosts Grass moves"));
        pokemon.setAbility2(null);  // ability2 assente
        pokemon.setHiddenAbility(null);  // hiddenAbility assente

        when(pokemonRepository.findById(2)).thenReturn(Optional.of(pokemon));

        // Act
        PokemonDto dto = pokemonService.getPokemonById(2);

        // Assert
        assertNotNull(dto);
        assertEquals(1, dto.id());
        assertEquals("Bulbasaur", dto.species());
        assertNotNull(dto.type1());
        assertEquals("Grass", dto.type1().name());
        assertNull(dto.type2());
        assertNotNull(dto.ability1());
        assertEquals("Overgrow", dto.ability1().name());
        assertNull(dto.ability2());
        assertNull(dto.hiddenAbility());
    }

    /**
     * Test Case: {@link NotFoundException} is thrown when a Pokémon is not found.
     */
    @Test
    void testGetPokemonById_notFound() {
        // Arrange: nessun Pokémon trovato
        when(pokemonRepository.findById(99999)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            pokemonService.getPokemonById(99999);
        });

        assertEquals("Pokemon with id 99999 not found", exception.getMessage());
    }
}
