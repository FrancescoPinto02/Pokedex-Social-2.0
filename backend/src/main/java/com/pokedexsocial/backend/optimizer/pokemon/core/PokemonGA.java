package com.pokedexsocial.backend.optimizer.pokemon.core;

import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeMultiplier;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Classe utilizzata per rappresentare un pokemon con tutte le sue caratteristiche
 */
public class PokemonGA {
    public static final int MIN_TOTAL_STATS = 175;
    public static final int MAX_TOTAL_STATS_STANDARD = 600;
    public static final int MAX_TOTAL_STATS_LEGENDARY = 780;


    private int number; //numero del pokedex nazionale
    private String name; //nome del Pokemon
    private PokemonType type1; //Primo Tipo del Pokemon
    private PokemonType type2; //Secondo Tipo del Pokemon (non tutti hanno un secondo tipo)
    private int total; //somma di tutte le statistiche
    private int hp; //Vita
    private int attack; //Attacco
    private int defense; //Difesa
    private int specialAttack; //Attacco Speciale
    private int specialDefense; //Difesa Speciale
    private int speed; //Velocità
    private PokemonRarity rarity; //Rarità


    private Set<PokemonTypeName> resistances; //Resistenze
    private Set<PokemonTypeName> weaknesses; //Debolezze


    // Costruttore
    public PokemonGA(
            int number,
            String name,
            PokemonType type1,
            PokemonType type2,
            int hp,
            int attack,
            int defense,
            int specialAttack,
            int specialDefense,
            int speed,
            PokemonRarity rarity
    ) {
        this.number = number;
        this.name = name;

        // --- Gestione null ---
        this.type1 = (type1 == null) ? new PokemonType(PokemonTypeName.UNDEFINED) : type1;
        this.type2 = (type2 == null) ? new PokemonType(PokemonTypeName.UNDEFINED) : type2;

        // --- Regola 1: Entrambi i tipi non possono essere UNDEFINED ---
        if (this.type1.getName() == PokemonTypeName.UNDEFINED &&
                this.type2.getName() == PokemonTypeName.UNDEFINED) {
            throw new IllegalArgumentException("A Pokemon must have at least one defined type");
        }

        // --- Regola 2: Se type1 è UNDEFINED ma type2 no, scambiali ---
        if (this.type1.getName() == PokemonTypeName.UNDEFINED &&
                this.type2.getName() != PokemonTypeName.UNDEFINED) {
            PokemonType temp = this.type1;
            this.type1 = this.type2;
            this.type2 = temp;
        }

        // --- Statistiche ---
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;

        // --- Calcolo automatico del totale ---
        this.total = hp + attack + defense + specialAttack + specialDefense + speed;

        this.rarity = rarity;

        // --- Calcolo resistenze e debolezze ---
        this.resistances = new HashSet<>();
        this.weaknesses = new HashSet<>();
        calculateResistances();
        calculateWeaknesses();
    }

    //Calcola automaticamente tutte le resistenze in base ai due Type del pokemon
    private void calculateResistances() {
        Set<PokemonTypeName> allTypes = EnumSet.allOf(PokemonTypeName.class);
        allTypes.remove(PokemonTypeName.UNDEFINED);

        Map<PokemonTypeName, Double> def1 = type1.getDefensiveProperties();
        Map<PokemonTypeName, Double> def2 = type2.getDefensiveProperties();

        boolean monotype = (type2.getName() == PokemonTypeName.UNDEFINED);

        for (PokemonTypeName attackType : allTypes) {
            double multiplier1 = def1.getOrDefault(attackType, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS);
            double multiplier2 = monotype
                    ? 1.0
                    : def2.getOrDefault(attackType, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS);

            double finalMultiplier = multiplier1 * multiplier2;

            if (finalMultiplier < PokemonTypeMultiplier.NORMAL_EFFECTIVENESS) {
                resistances.add(attackType);
            }
        }
    }

    private void calculateWeaknesses() {
        Set<PokemonTypeName> allTypes = EnumSet.allOf(PokemonTypeName.class);
        allTypes.remove(PokemonTypeName.UNDEFINED);

        Map<PokemonTypeName, Double> def1 = type1.getDefensiveProperties();
        Map<PokemonTypeName, Double> def2 = type2.getDefensiveProperties();

        boolean monotype = (type2.getName() == PokemonTypeName.UNDEFINED);

        for (PokemonTypeName attackType : allTypes) {
            double m1 = def1.getOrDefault(attackType, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS);
            double m2 = monotype
                    ? 1.0
                    : def2.getOrDefault(attackType, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS);

            double finalMultiplier = m1 * m2;

            // Qualsiasi moltiplicatore > 1.0 è una debolezza (es. 2x, 4x)
            if (finalMultiplier > PokemonTypeMultiplier.NORMAL_EFFECTIVENESS) {
                weaknesses.add(attackType);
            }
        }
    }


    //Getter and Setters
    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PokemonType getType1() {
        return type1;
    }

    public void setType1(PokemonType type1) {
        this.type1 = type1;
    }

    public PokemonType getType2() {
        return type2;
    }

    public void setType2(PokemonType type2) {
        this.type2 = type2;
    }

    public void setResistances(Set<PokemonTypeName> resistances) {
        this.resistances = resistances;
    }

    public void setWeaknesses(Set<PokemonTypeName> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getSpecialAttack() {
        return specialAttack;
    }

    public void setSpecialAttack(int specialAttack) {
        this.specialAttack = specialAttack;
    }

    public int getSpecialDefense() {
        return specialDefense;
    }

    public void setSpecialDefense(int specialDefense) {
        this.specialDefense = specialDefense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public PokemonRarity getRarity() {
        return rarity;
    }

    public void setRarity(PokemonRarity rarity) {
        this.rarity = rarity;
    }

    public Set<PokemonTypeName> getResistances() {
        return resistances;
    }

    public Set<PokemonTypeName> getWeaknesses() {
        return weaknesses;
    }

    public boolean isMegaEvolution(){
        //Meganium è l`unico pokemon che contiene Mega nel nome ma non è una Megaevoluzione
        if(name.contains("Mega") && !name.equals("Meganium")){
            return true;
        }
        else{
            return false;
        }
    }

    //Utility
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PokemonGA pokemon = (PokemonGA) o;
        return number == pokemon.number && Objects.equals(name, pokemon.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, name);
    }

    @Override
    public String toString() {
        return "#" + number + " " + name;
    }
}

