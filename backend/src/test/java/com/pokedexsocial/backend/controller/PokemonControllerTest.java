package com.pokedexsocial.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.PokemonFiltersDto;
import com.pokedexsocial.backend.dto.PokemonListDto;
import com.pokedexsocial.backend.service.PokemonService;
import com.pokedexsocial.backend.specification.PokemonSearchCriteria;
import com.pokedexsocial.backend.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.domain.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PokemonController.class)
@ContextConfiguration(classes = {PokemonController.class, PokemonControllerTest.TestConfig.class})
class PokemonControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        PokemonService pokemonService() {
            return Mockito.mock(PokemonService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PokemonService pokemonService;

    @Autowired
    private ObjectMapper objectMapper;

    private PokemonDto samplePokemon;
    private PokemonFiltersDto filtersDto;

    @BeforeEach
    void setup() {
        samplePokemon = new PokemonDto();
        samplePokemon.setId(1);
        samplePokemon.setSpecies("Bulbasaur");
        samplePokemon.setNdex(1);

        filtersDto = new PokemonFiltersDto(
                List.of(),
                List.of(),
                new Range<>(1, 1025),
                new Range<>(null, null),
                new Range<>(null, null)
        );
    }

    // --------------------------------------------------------------------
    // GET /pokemon/{id}
    // --------------------------------------------------------------------

    @Test
    void getPokemonById_ShouldReturnOkAndPokemon() throws Exception {
        when(pokemonService.getPokemonById(1)).thenReturn(samplePokemon);

        mockMvc.perform(get("/pokemon/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.species").value("Bulbasaur"))
                .andExpect(jsonPath("$.ndex").value(1));

        verify(pokemonService).getPokemonById(1);
    }


    // --------------------------------------------------------------------
    // GET /pokemon (search)
    // --------------------------------------------------------------------

    @Test
    void search_ShouldReturnPagedResponse() throws Exception {
        PokemonListDto listDto = new PokemonListDto(
                1,
                1,
                "Bulbasaur",
                null,
                "Seed Pokémon",
                List.of(),
                "http://image.url"
        );

        Page<PokemonListDto> page = new PageImpl<>(
                List.of(listDto),
                PageRequest.of(0, 20, Sort.by("ndex").ascending()),
                1
        );

        when(pokemonService.search(any(PokemonSearchCriteria.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/pokemon")
                        .param("species", "Bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].species").value("Bulbasaur"))
                .andExpect(jsonPath("$.items[0].ndex").value(1))
                .andExpect(jsonPath("$.items[0].pokemonClass").value("Seed Pokémon"))
                .andExpect(jsonPath("$.items[0].imageUrl").value("http://image.url"));

        verify(pokemonService).search(any(PokemonSearchCriteria.class), any(Pageable.class));
    }

    @Test
    void search_ShouldForceSafeSort_WhenSortFieldIsNotWhitelisted() throws Exception {
        Page<PokemonListDto> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20, Sort.by("ndex")),
                0
        );

        when(pokemonService.search(any(PokemonSearchCriteria.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/pokemon")
                        .param("sort", "nonexistentField,desc"))
                .andExpect(status().isOk());

        // Validate the sort was replaced correctly (ndex ASC)
        verify(pokemonService, Mockito.atLeastOnce())
                .search(any(PokemonSearchCriteria.class), argThat(pageable ->
                        pageable.getSort().getOrderFor("ndex") != null &&
                                pageable.getSort().getOrderFor("ndex").getDirection() == Sort.Direction.ASC
                )
        );
    }

    // --------------------------------------------------------------------
    // GET /pokemon/filters
    // --------------------------------------------------------------------

    @Test
    void getFilters_ShouldReturnFiltersDto() throws Exception {
        when(pokemonService.getFilters()).thenReturn(filtersDto);

        mockMvc.perform(get("/pokemon/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ndexRange.min").value(1))
                .andExpect(jsonPath("$.ndexRange.max").value(1025));

        verify(pokemonService).getFilters();
    }
}

