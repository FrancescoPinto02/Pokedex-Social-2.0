package com.pokedexsocial.backend.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.util.Objects;

/**
 * Entity that represents a Pokémon type (e.g., Grass, Fire, Water).
 * Used to define Pokémon primary and secondary types.
 */
@Entity
@Table(name = "type")
public class Type {

    /** ID of the Type */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Name of the type (unique), e.g., "Fire" */
    @Column(nullable = false, unique = true)
    private String name;

    // Constructors
    public Type() {
    }

    public Type(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Utility
    @Override
    public String toString() {
        return "Type{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof Type type)) {
            return false;
        }
        return Objects.equals(id, type.id) && Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
