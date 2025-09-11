package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Pokémon entities.
 * Extends JpaRepository for basic CRUD operations
 * and JpaSpecificationExecutor for dynamic queries.
 */
@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer>, JpaSpecificationExecutor<Pokemon> {
}
