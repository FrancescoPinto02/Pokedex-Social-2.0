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


    //Costruttori
    public PokemonGA(int number, String name, PokemonType type1, PokemonType type2, int total, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, PokemonRarity rarity) {
        this.number = number;
        this.name = name;
        this.type1 = (type1==null) ? new PokemonType(PokemonTypeName.UNDEFINED) : type1;
        this.type2 = (type2==null) ? new PokemonType(PokemonTypeName.UNDEFINED) : type2;
        this.total = (total==(hp + attack + defense + specialAttack + specialDefense + speed)) ? total : (hp + attack + defense + specialAttack + specialDefense + speed);
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.specialAttack = specialAttack;
        this.specialDefense = specialDefense;
        this.speed = speed;
        this.rarity = rarity;

        resistances = new HashSet<>();
        weaknesses = new HashSet<>();
        calculateResistances();
        calculateWeaknesses();
    }

    public PokemonGA(int number, String name, PokemonType type1, PokemonType type2, int hp, int attack, int defense, int specialAttack, int specialDefense, int speed, PokemonRarity rarity) {
        this(number, name, type1, type2, (hp + attack + defense + specialAttack + specialDefense + speed), hp, attack, defense, specialAttack, specialDefense, speed, rarity);
    }

    //Calcola automaticamente tutte le resistenze in base ai due Type del pokemon
    private void calculateResistances(){
        Set<PokemonTypeName> allTypes = EnumSet.allOf(PokemonTypeName.class);
        allTypes.remove(PokemonTypeName.UNDEFINED);
        Map<PokemonTypeName, Double> defProperties1 = type1.getDefensiveProperties();
        boolean monotype = type2.getName() == PokemonTypeName.UNDEFINED;

        if(monotype){
            for(PokemonTypeName x : allTypes){
                Double mul1 = defProperties1.get(x);
                if(mul1.equals(PokemonTypeMultiplier.IMMUNE_TO) || mul1.equals(PokemonTypeMultiplier.RESISTS)){
                    resistances.add(x);
                }
            }
        }
        else{
            Map<PokemonTypeName, Double> defProperties2 = type2.getDefensiveProperties();

            for(PokemonTypeName x : allTypes){
                Double mul1 = defProperties1.get(x);
                Double mul2 = defProperties2.get(x);

                if(mul1.equals(PokemonTypeMultiplier.IMMUNE_TO) || mul2.equals(PokemonTypeMultiplier.IMMUNE_TO)){
                    resistances.add(x);
                }
                else if(mul1.equals(PokemonTypeMultiplier.NORMAL_EFFECTIVENESS) && mul2.equals(PokemonTypeMultiplier.RESISTS)){
                    resistances.add(x);
                }
                else if(mul1.equals(PokemonTypeMultiplier.RESISTS)){
                    if(mul2.equals(PokemonTypeMultiplier.RESISTS) || mul2.equals(PokemonTypeMultiplier.NORMAL_EFFECTIVENESS)){
                        resistances.add(x);
                    }
                }
            }
        }
    }

    //Calcola automaticamente tutte le debolezze in base ai due type del pokemon
    private void calculateWeaknesses(){
        Set<PokemonTypeName> allTypes = EnumSet.allOf(PokemonTypeName.class);
        allTypes.remove(PokemonTypeName.UNDEFINED);
        Map<PokemonTypeName, Double> defProperties1 = type1.getDefensiveProperties();
        boolean monotype = type2.getName() == PokemonTypeName.UNDEFINED;

        if(monotype){
            for(PokemonTypeName x : allTypes){
                Double mul1 = defProperties1.get(x);
                if(mul1.equals(PokemonTypeMultiplier.WEAK_TO)){
                    weaknesses.add(x);
                }
            }
        }
        else{
            Map<PokemonTypeName, Double> defProperties2 = type2.getDefensiveProperties();

            for(PokemonTypeName x : allTypes){
                Double mul1 = defProperties1.get(x);
                Double mul2 = defProperties2.get(x);

                if(mul1.equals(PokemonTypeMultiplier.WEAK_TO)){
                    if(mul2.equals(PokemonTypeMultiplier.WEAK_TO) || mul2.equals(PokemonTypeMultiplier.NORMAL_EFFECTIVENESS)){
                        weaknesses.add(x);
                    }
                }
                else if(mul1.equals(PokemonTypeMultiplier.NORMAL_EFFECTIVENESS) && mul2.equals(PokemonTypeMultiplier.WEAK_TO)){
                    weaknesses.add(x);
                }
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

