package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface for Pokémon entities.
 * Extends JpaRepository for basic CRUD operations
 * and JpaSpecificationExecutor for dynamic queries.
 */
@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, Integer>, JpaSpecificationExecutor<Pokemon> {

    @Query("SELECT MIN(p.ndex) FROM Pokemon p")
    Integer findMinNdex();

    @Query("SELECT MAX(p.ndex) FROM Pokemon p")
    Integer findMaxNdex();

    @Query("SELECT MIN(p.weight) FROM Pokemon p")
    BigDecimal findMinWeight();

    @Query("SELECT MAX(p.weight) FROM Pokemon p")
    BigDecimal findMaxWeight();

    @Query("SELECT MIN(p.height) FROM Pokemon p")
    BigDecimal findMinHeight();

    @Query("SELECT MAX(p.height) FROM Pokemon p")
    BigDecimal findMaxHeight();

    @Query("SELECT p FROM Pokemon p " +
            "JOIN FETCH p.type1 " +
            "LEFT JOIN FETCH p.type2")
    List<Pokemon> findAllWithTypes();
}
