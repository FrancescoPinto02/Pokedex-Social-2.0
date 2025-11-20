package com.pokedexsocial.backend.benchmark.stub;

import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;

import java.util.Set;

public class PokemonExportDTO {

    public int number;
    public String name;
    public PokemonTypeName type1;
    public PokemonTypeName type2;
    public int hp;
    public int attack;
    public int defense;
    public int specialAttack;
    public int specialDefense;
    public int speed;
    public PokemonRarity rarity;

    public Set<PokemonTypeName> resistances;
    public Set<PokemonTypeName> weaknesses;
}
