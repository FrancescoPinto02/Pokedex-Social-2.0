package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.PagedResponse;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.PokemonFiltersDto;
import com.pokedexsocial.backend.dto.PokemonListDto;
import com.pokedexsocial.backend.service.PokemonService;
import com.pokedexsocial.backend.specification.PokemonSearchCriteria;
import com.pokedexsocial.backend.util.SortWhitelist;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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

    /**
     * Searches for Pokémon based on the given criteria with pagination and sorting.
     * <p>
     * Only "ndex" and "species" fields are allowed for sorting; other sort fields
     * are ignored and replaced with the default sort by "ndex" ascending.
     *
     * @param criteria the search criteria (validated)
     * @param pageable pagination and sorting information
     * @return a paged response containing the list of matching Pokémon
     */
    @GetMapping
    public PagedResponse<PokemonListDto> search(
            @Valid @ModelAttribute PokemonSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "ndex", direction = Sort.Direction.ASC) Pageable pageable) {

        Sort safeSort = SortWhitelist.filter(
                pageable.getSort(),
                Set.of("ndex", "species"),
                Sort.by("ndex").ascending()
        );

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
        Page<PokemonListDto> result = pokemonService.search(criteria, safePageable);
        return PagedResponse.from(result);
    }

    @GetMapping("/filters")
    public ResponseEntity<PokemonFiltersDto> getFilters() {
        PokemonFiltersDto filters = pokemonService.getFilters();
        return ResponseEntity.ok(filters);
    }
}
