package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Team;
import com.pokedexsocial.backend.model.TeamPokemon;
import com.pokedexsocial.backend.model.TeamPokemonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamPokemonRepository extends JpaRepository<TeamPokemon, TeamPokemonId> {

    List<TeamPokemon> findByTeam(Team team);
}
