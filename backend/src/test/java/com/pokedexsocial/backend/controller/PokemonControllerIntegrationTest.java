package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.service.PokemonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

//TODO: Sostituire DB reale con un DB Dedicato al test

/**
 * Integration tests for {@link PokemonController} using the real PostgreSQL database.
 * Tests run in a transaction that rolls back automatically after each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PokemonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetPokemonById_existingPokemon() throws Exception {
        mockMvc.perform(get("/pokemon/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.species").value("Bulbasaur"))
                .andExpect(jsonPath("$.type1.name").isNotEmpty())
                .andExpect(jsonPath("$.ability1.name").isNotEmpty());
    }

    @Test
    void testGetPokemonById_notFound() throws Exception {
        int nonExistingId = 99999;

        mockMvc.perform(get("/pokemon/" + nonExistingId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pokemon with id " + nonExistingId + " not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetPokemonById_invalidId() throws Exception {
        // ID non valido secondo @Min(1)
        mockMvc.perform(get("/pokemon/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
