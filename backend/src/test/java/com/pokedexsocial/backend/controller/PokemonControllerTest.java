package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.AbilityDto;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.TypeDto;
import com.pokedexsocial.backend.exception.NotFoundException;
import com.pokedexsocial.backend.service.PokemonService;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link PokemonController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PokemonService pokemonService;

    /**
     * Test Case: correct JSON response received when the
     * Pokèmon ID is valid and the pokèmon exists
     */
    @Test
    void testGetPokemonById_Valid() throws Exception {
        PokemonDto pokemonDto = new PokemonDto(
                1, 1, "Bulbasaur", null, null, null,
                new TypeDto(1, "Grass"), new TypeDto(2, "Poison"),
                new AbilityDto(1, "Overgrow", "Boosts Grass moves"),
                null, null,
                45, 49, 49, 65, 65, 45, 318,
                BigDecimal.valueOf(6.9), BigDecimal.valueOf(0.7),
                "Seed Pokémon", BigDecimal.valueOf(88.0), BigDecimal.valueOf(12.0),
                "Monster", "Grass", "/images/bulbasaur.png"
        );

        when(pokemonService.getPokemonById(1)).thenReturn(pokemonDto);

        mockMvc.perform(get("/pokemon/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.species").value("Bulbasaur"))
                .andExpect(jsonPath("$.type1.name").value("grass"))
                .andExpect(jsonPath("$.type2.name").value("poison"))
                .andExpect(jsonPath("$.ability1.name").value("Overgrow"));
    }

    /**
     * Test Case: {@link NotFoundException} is thrown when the
     * Pokèmon ID is valid but the Pokèmon does not exist
     */
    @Test
    void testGetPokemonById_NotFound() throws Exception {
        when(pokemonService.getPokemonById(99999)).thenThrow(new NotFoundException("Pokemon with id 99999 not found"));

        mockMvc.perform(get("/pokemon/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("Pokemon with id 99999 not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    /**
     * Test Case: Response Bad Request - 400 when the
     * Pokèmon ID is not valid
     */
    @Test
    void testGetPokemonById_InvalidId() throws Exception {
        // Usa id = 0, invalido secondo @Min(1)
        mockMvc.perform(get("/pokemon/0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
