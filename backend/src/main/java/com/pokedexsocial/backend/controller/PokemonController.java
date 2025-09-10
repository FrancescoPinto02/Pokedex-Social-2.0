package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.service.PokemonService;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for Pokémon catalog endpoints. */
@RestController
@RequestMapping("/pokemon")
@Validated
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    /**
     * Retrieves a Pokémon by its unique ID.
     *
     * @param id database identifier of the Pokémon (must be >= 1)
     * @return PokemonDto with complete details
     */
    @GetMapping("/{id}")
    public ResponseEntity<PokemonDto> getPokemonById(@PathVariable @Min(1) Integer id) {
        PokemonDto pokemon = pokemonService.getPokemonById(id);
        return ResponseEntity.ok(pokemon);
    }
}
