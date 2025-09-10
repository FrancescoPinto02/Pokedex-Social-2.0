package com.pokedexsocial.backend.service;


import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

//TODO: Modificare per utilizzare un DB dedicato ai Test

/**
 * Integration tests for {@link PokemonService} using the real PostgreSQL database.
 * Tests run in a transaction that rolls back automatically after each test.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PokemonServiceIntegrationTest {

    @Autowired
    private PokemonService pokemonService;

    @Test
    void testGetPokemonById_existingPokemon() {
        int existingId = 1;

        PokemonDto dto = pokemonService.getPokemonById(existingId);

        assertNotNull(dto);
        assertEquals(existingId, dto.id());
        assertEquals("Bulbasaur", dto.species());
        assertNotNull(dto.type1());
        assertNotNull(dto.ability1());
    }

    @Test
    void testGetPokemonById_notFound() {
        int nonExistingId = 99999;

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            pokemonService.getPokemonById(nonExistingId);
        });

        assertEquals("Pokemon with id " + nonExistingId + " not found", exception.getMessage());
    }

}
