package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Ability;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Ability entities.
 * Extends JpaRepository for basic CRUD operations.
 */
public interface AbilityRepository extends JpaRepository<Ability, Integer> {
}
