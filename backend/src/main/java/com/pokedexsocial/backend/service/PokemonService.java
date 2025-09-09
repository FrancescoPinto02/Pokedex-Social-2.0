package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.AbilityDto;
import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.TypeDto;
import com.pokedexsocial.backend.exception.NotFoundException;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.repository.PokemonRepository;
import org.springframework.stereotype.Service;

@Service
public class PokemonService {

    private final PokemonRepository pokemonRepository;

    public PokemonService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    public PokemonDto getPokemonById(Long id) {
        Pokemon pokemon = pokemonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pokemon with id " + id + " not found"));

        return mapToDto(pokemon);
    }

    private PokemonDto mapToDto(Pokemon p) {
        return new PokemonDto(
                p.getId(),
                p.getNdex(),
                p.getSpecies(),
                p.getForme(),
                p.getDex1(),
                p.getDex2(),
                p.getType1() != null ? new TypeDto(p.getType1().getId(), p.getType1().getName()) : null,
                p.getType2() != null ? new TypeDto(p.getType2().getId(), p.getType2().getName()) : null,
                p.getAbility1() != null ? new AbilityDto(p.getAbility1().getId(), p.getAbility1().getName(), p.getAbility1().getDescription()) : null,
                p.getAbility2() != null ? new AbilityDto(p.getAbility2().getId(), p.getAbility2().getName(), p.getAbility2().getDescription()) : null,
                p.getHiddenAbility() != null ? new AbilityDto(p.getHiddenAbility().getId(), p.getHiddenAbility().getName(), p.getHiddenAbility().getDescription()) : null,
                p.getHp(),
                p.getAttack(),
                p.getDefense(),
                p.getSpattack(),
                p.getSpdefense(),
                p.getSpeed(),
                p.getTotal(),
                p.getWeight(),
                p.getHeight(),
                p.getPokemonClass(),
                p.getPercentMale(),
                p.getPercentFemale(),
                p.getEggGroup1(),
                p.getEggGroup2(),
                p.getImageUrl()
        );
    }
}
