package com.pokedexsocial.backend.optimizer.pokemon.type;

/**
 * Classe che fornisce costanti statiche che rappresentano i moltiplicatori offensivi
 * e difensivi delle lotte nel gioco Pokemon
 */
public final class PokemonTypeMultiplier {
    //Moltiplicatori offensivi
    public static final Double SUPER_EFFECTIVE = 2.0; //danno inflitto 2x
    public static final Double NOT_VERY_EFFECTIVE = 0.5; //danno inflitto 0.5x
    public static final Double NO_EFFECT = 0.0; //danno inflitto 0x

    //Moltiplicatori difensivi
    public static final Double WEAK_TO = 2.0; //danno subito 2x
    public static final Double RESISTS = 0.5; //danno subito 0.5x
    public static final Double IMMUNE_TO = 0.0; //damage subito 0x

    //Moltiplicatori neutrali
    public static final Double NORMAL_EFFECTIVENESS = 1.0; //danno inflitto/subito 1x
}
