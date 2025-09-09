package com.pokedexsocial.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.util.Objects;

/**
 * Represents a Pokémon ability.
 * Abilities are special passive effects that Pokémon can have in battle
 * (e.g., Overgrow, Chlorophyll).
 */
@Entity
@Table(name = "ability")
public class Ability {

    /** ID of the Ability */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Ability name (unique, e.g., Overgrow). */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Description of the ability's effect. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // Constructors
    public Ability() {
    }

    public Ability(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ability)) return false;
        Ability ability = (Ability) o;
        return Objects.equals(id, ability.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
