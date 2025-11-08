package com.pokedexsocial.backend.optimizer.pokemon.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PokemonTypeTest {

    private Map<PokemonTypeName, Double> offensive;
    private Map<PokemonTypeName, Double> defensive;

    @BeforeEach
    void setUp() {
        offensive = new EnumMap<>(PokemonTypeName.class);
        defensive = new EnumMap<>(PokemonTypeName.class);
        offensive.put(PokemonTypeName.FIRE, 2.0);
        defensive.put(PokemonTypeName.WATER, 0.5);
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameReference() {
        PokemonType type = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(type.equals(type)).isTrue();
    }

    @Test
    void equals_ShouldReturnFalse_WhenOtherIsNull() {
        PokemonType type = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(type.equals(null)).isFalse();
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentClass() {
        PokemonType type = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(type.equals("NotAPokemonType")).isFalse();
    }

    @Test
    void equals_ShouldReturnTrue_WhenSameName() {
        PokemonType t1 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        PokemonType t2 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(t1.equals(t2)).isTrue();
    }

    @Test
    void equals_ShouldReturnFalse_WhenDifferentName() {
        PokemonType t1 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        PokemonType t2 = new PokemonType(PokemonTypeName.WATER, offensive, defensive);
        assertThat(t1.equals(t2)).isFalse();
    }

    @Test
    void hashCode_ShouldBeEqual_ForEqualObjects() {
        PokemonType t1 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        PokemonType t2 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
    }

    @Test
    void hashCode_ShouldDiffer_ForDifferentNames() {
        PokemonType t1 = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        PokemonType t2 = new PokemonType(PokemonTypeName.WATER, offensive, defensive);
        assertThat(t1.hashCode()).isNotEqualTo(t2.hashCode());
    }

    @Test
    void toString_ShouldReturnNameAsString() {
        PokemonType type = new PokemonType(PokemonTypeName.FIRE, offensive, defensive);
        assertThat(type.toString()).isEqualTo("FIRE");
    }
}

