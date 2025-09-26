package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.AbilityDto;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.TypeDto;
import com.pokedexsocial.backend.exception.NotFoundException;
import com.pokedexsocial.backend.service.PokemonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link PokemonController}.
 */
@WebMvcTest(PokemonController.class)
@AutoConfigureMockMvc(addFilters = false) // disattiva i filtri Security se presenti
class PokemonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PokemonService pokemonService; // <-- mock registrato nel contesto Spring

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

        mockMvc.perform(get("/pokemon/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.species").value("Bulbasaur"))
                // ATTENZIONE: nel DTO hai "Grass"/"Poison" (maiuscole);
                // quindi l'assert deve combaciare:
                .andExpect(jsonPath("$.type1.name").value("Grass"))
                .andExpect(jsonPath("$.type2.name").value("Poison"))
                .andExpect(jsonPath("$.ability1.name").value("Overgrow"));
    }

    @Test
    void testGetPokemonById_NotFound() throws Exception {
        when(pokemonService.getPokemonById(99999))
                .thenThrow(new NotFoundException("Pokemon with id 99999 not found"));

        mockMvc.perform(get("/pokemon/99999").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Pokemon with id 99999 not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testGetPokemonById_InvalidId() throws Exception {
        // Richiede che il controller validi @PathVariable con @Min(1) o simili
        mockMvc.perform(get("/pokemon/0").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
