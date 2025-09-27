package com.pokedexsocial.backend.repository;

import com.pokedexsocial.backend.model.Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Type entities.
 * Extends JpaRepository for basic CRUD operations.
 */
public interface TypeRepository extends JpaRepository<Type, Integer> {
    Optional<Type> findByName(String name);
}
