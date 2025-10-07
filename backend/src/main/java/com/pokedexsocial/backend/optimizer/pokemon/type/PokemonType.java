package com.pokedexsocial.backend.optimizer.pokemon.type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Classe che rappresenta il tipo di un Pokemon con informazioni sul nome del tipo,
 * le proprietà offensive e le proprietà difensive
 */
public class PokemonType {
    //@ spec_public
    private PokemonTypeName name; //Nome del tipo
    //@ spec_public
    private Map<PokemonTypeName, Double> offensiveProperties; //Moltiplicatori offensivi contro gli altri tipi
    //@ spec_public
    private Map<PokemonTypeName, Double> defensiveProperties;  //Moltiplicatori difensivi contro gli altri tipi

/*@
  @ public invariant name != null;
  @ public invariant offensiveProperties != null;
  @ public invariant defensiveProperties != null;
  @
  @ // Quando il Tipo è UNDEFINED non deve avere debolezze o resistenze
  @ public invariant (name == PokemonTypeName.UNDEFINED) ==> (offensiveProperties.isEmpty() && defensiveProperties.isEmpty());
  @ public invariant (name != PokemonTypeName.UNDEFINED) ==> (!offensiveProperties.isEmpty() && !defensiveProperties.isEmpty());
  @*/


    /*@
      @ // Precondizioni
      @ requires name != null;
      @ requires offensiveProperties != null;
      @ requires defensiveProperties != null;
      @ requires (name == PokemonTypeName.UNDEFINED) ==> (offensiveProperties.isEmpty() && defensiveProperties.isEmpty());
      @ requires (name != PokemonTypeName.UNDEFINED) ==> (!offensiveProperties.isEmpty() && !defensiveProperties.isEmpty());
      @
      @ // Postcondizioni
      @ ensures this.name == name;
      @ ensures this.offensiveProperties == offensiveProperties;
      @ ensures this.defensiveProperties == defensiveProperties;
      @*/
    public PokemonType(PokemonTypeName name,
                       Map<PokemonTypeName, Double> offensiveProperties,
                       Map<PokemonTypeName, Double> defensiveProperties) {
        this.name = name;
        this.offensiveProperties = offensiveProperties;
        this.defensiveProperties = defensiveProperties;
    }

    /*@
      @ // Precondizioni
      @ requires name != null;
      @ requires name == PokemonTypeName.UNDEFINED;
      @
      @ // Postcondizioni
      @ ensures this.name == name;
      @ ensures this.offensiveProperties != null;
      @ ensures this.defensiveProperties != null;
      @*/
    public PokemonType(PokemonTypeName name) {
        this(name, new HashMap<PokemonTypeName, Double>(), new HashMap<PokemonTypeName, Double>());
    }


    //Getters e Setters
    /*@ public normal_behavior
      @   assignable \nothing;
      @   ensures \result == name;
      @*/
    public /*@ pure @*/ PokemonTypeName getName() {
        return name;
    }

    /*@ public normal_behavior
      @   requires name!=null;
      @   requires (name == PokemonTypeName.UNDEFINED && offensiveProperties.isEmpty() && defensiveProperties.isEmpty()) ||
      @            (name != PokemonTypeName.UNDEFINED && !offensiveProperties.isEmpty() && !defensiveProperties.isEmpty());
      @   assignable this.name;
      @   ensures this.name == name;
      @*/
    public void setName(PokemonTypeName name) {
        this.name = name;
    }

    /*@ public normal_behavior
      @   assignable \nothing;
      @   ensures \result == offensiveProperties;
      @*/
    public /*@ pure @*/ Map<PokemonTypeName, Double> getOffensiveProperties() {
        return offensiveProperties;
    }

    /*@ public normal_behavior
      @   requires offensiveProperties!=null;
      @   requires (name == PokemonTypeName.UNDEFINED && offensiveProperties.isEmpty()) ||
      @            (name != PokemonTypeName.UNDEFINED && !offensiveProperties.isEmpty());
      @   assignable this.offensiveProperties;
      @   ensures this.offensiveProperties == offensiveProperties;
      @*/
    public void setOffensiveProperties(Map<PokemonTypeName, Double> offensiveProperties) {
        this.offensiveProperties = offensiveProperties;
    }

    /*@ public normal_behavior
      @   assignable \nothing;
      @   ensures \result == defensiveProperties;
      @*/
    public /*@ pure @*/ Map<PokemonTypeName, Double> getDefensiveProperties() {
        return defensiveProperties;
    }

    /*@ public normal_behavior
      @   requires defensiveProperties!=null;
      @   requires (name == PokemonTypeName.UNDEFINED && defensiveProperties.isEmpty()) ||
      @            (name != PokemonTypeName.UNDEFINED && !defensiveProperties.isEmpty());
      @   assignable this.defensiveProperties;
      @   ensures this.defensiveProperties == defensiveProperties;
      @*/
    public void setDefensiveProperties(Map<PokemonTypeName, Double> defensiveProperties) {
        this.defensiveProperties = defensiveProperties;
    }

    //Utility
    @Override
    //@ skipesc
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PokemonType that = (PokemonType) o;
        return name == that.name;
    }

    @Override
    //@ skipesc
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    //@ skipesc
    public String toString() {
        return "" + name;
    }
}
