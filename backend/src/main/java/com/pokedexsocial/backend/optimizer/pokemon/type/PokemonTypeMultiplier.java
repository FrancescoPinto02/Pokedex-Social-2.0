package com.pokedexsocial.backend.optimizer.pokemon.type;

/**
 * Classe che fornisce costanti statiche che rappresentano i moltiplicatori offensivi
 * e difensivi delle lotte nel gioco Pokemon
 */
public final class PokemonTypeMultiplier {
    //Moltiplicatori offensivi
    public static final double SUPER_EFFECTIVE = 2.0; //danno inflitto 2x
    public static final double NOT_VERY_EFFECTIVE = 0.5; //danno inflitto 0.5x
    public static final double NO_EFFECT = 0.0; //danno inflitto 0x

    //Moltiplicatori difensivi
    public static final double WEAK_TO = 2.0; //danno subito 2x
    public static final double RESISTS = 0.5; //danno subito 0.5x
    public static final double IMMUNE_TO = 0.0; //damage subito 0x

    //Moltiplicatori neutrali
    public static final double NORMAL_EFFECTIVENESS = 1.0; //danno inflitto/subito 1x


    /*@ //I valori questi sono nel gioco e non possono cambiare
      @ public invariant SUPER_EFFECTIVE == 2.0;
      @ public invariant NOT_VERY_EFFECTIVE == 0.5;
      @ public invariant NO_EFFECT == 0.0;
      @ public invariant WEAK_TO == 2.0;
      @ public invariant RESISTS == 0.5;
      @ public invariant IMMUNE_TO == 0.0;
      @ public invariant NORMAL_EFFECTIVENESS == 1.0;
      @
      @ //Sono ovvie perchè i valori non cambiano (per ora)
      @ public invariant SUPER_EFFECTIVE > NOT_VERY_EFFECTIVE && NOT_VERY_EFFECTIVE > NO_EFFECT;
      @ public invariant WEAK_TO > RESISTS && RESISTS > IMMUNE_TO;
      @
      @*/
}
