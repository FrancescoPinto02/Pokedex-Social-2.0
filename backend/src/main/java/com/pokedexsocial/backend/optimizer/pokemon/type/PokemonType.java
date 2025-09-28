package com.pokedexsocial.backend.optimizer.pokemon.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Classe che rappresenta il tipo di un Pokemon con informazioni sul nome del tipo,
 * le proprietà offensive e le proprietà difensive
 */
public class PokemonType {
    private PokemonTypeName name; //Nome del tipo
    private Map<PokemonTypeName, Double> offensiveProperties; //Moltiplicatori offensivi contro gli altri tipi
    private Map<PokemonTypeName, Double> defensiveProperties;  //Moltiplicatori difensivi contro gli altri tipi

    //Costruttori
    public PokemonType(PokemonTypeName name, Map<PokemonTypeName, Double> offensiveProperties, Map<PokemonTypeName, Double> defensiveProperties) {
        this.name = name;
        this.offensiveProperties = offensiveProperties;
        this.defensiveProperties = defensiveProperties;
    }

    public PokemonType(PokemonTypeName name) {
        this(name, new HashMap<>(), new HashMap<>());
    }


    //Getters e Setters
    public PokemonTypeName getName() {
        return name;
    }

    public void setName(PokemonTypeName name) {
        this.name = name;
    }

    public Map<PokemonTypeName, Double> getOffensiveProperties() {
        return offensiveProperties;
    }

    public void setOffensiveProperties(Map<PokemonTypeName, Double> offensiveProperties) {
        this.offensiveProperties = offensiveProperties;
    }

    public Map<PokemonTypeName, Double> getDefensiveProperties() {
        return defensiveProperties;
    }

    public void setDefensiveProperties(Map<PokemonTypeName, Double> defensiveProperties) {
        this.defensiveProperties = defensiveProperties;
    }

    //Utility
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PokemonType that = (PokemonType) o;
        return name == that.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "" + name;
    }
}
