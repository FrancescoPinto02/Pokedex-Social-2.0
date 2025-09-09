package com.pokedexsocial.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a Pokémon entry in the Pokédex.
 * Stores stats, classification, typing, abilities and other
 * descriptive attributes.
 */
@Entity
@Table(name = "pokemon")
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** National Pokédex number (unique identifier for the species). */
    @Column(name = "ndex", nullable = false)
    private Integer ndex;

    /** Pokémon species name (e.g., Bulbasaur, Charmander). */
    @Column(name = "species", nullable = false, length = 100)
    private String species;

    /** Forme variant (e.g., Mega, Alolan, Galarian). */
    @Column(name = "forme", length = 100)
    private String forme;

    /** Primary Pokédex description. */
    @Column(name = "dex1", nullable = false, columnDefinition = "TEXT")
    private String dex1;

    /** Secondary Pokédex description. */
    @Column(name = "dex2", nullable = false, columnDefinition = "TEXT")
    private String dex2;

    /** First type of the Pokémon. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type1_id", nullable = false)
    private Type type1;

    /** Second type of the Pokémon (nullable). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type2_id")
    private Type type2;

    /** Primary ability of the Pokémon. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability1_id", nullable = false)
    private Ability ability1;

    /** Secondary ability of the Pokémon (nullable). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ability2_id")
    private Ability ability2;

    /** Hidden ability of the Pokémon (nullable). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hidden_ability_id")
    private Ability hiddenAbility;

    // Base stats
    /** Pokèmon Health Points */
    @Column(name = "hp", nullable = false)
    private int hp;

    /** Pokèmon Attack */
    @Column(name = "attack", nullable = false)
    private int attack;

    /** Pokèmon Defense */
    @Column(name = "defense", nullable = false)
    private int defense;

    /** Pokèmon Special Attack */
    @Column(name = "spattack", nullable = false)
    private int spattack;

    /** Pokèmon Special Defense */
    @Column(name = "spdefense", nullable = false)
    private int spdefense;

    /** Pokèmon Speed */
    @Column(name = "speed", nullable = false)
    private int speed;

    /** Pokèmon Total Stats (Hp + Attack + Defense + Sp. Attack + Sp. Defense + Speed) */
    @Column(name = "total", nullable = false)
    private int total;

    /** Pokèmon Weight (Lbs) */
    @Column(name = "weight")
    private BigDecimal weight;

    /** Pokèmon Height (Feet and Inches) */
    @Column(name = "height")
    private BigDecimal height;

    /** Pokémon classification (e.g., Seed Pokémon). */
    @Column(name = "class", nullable = false)
    private String pokemonClass;

    /** Male appearance ratio (0.0 - 1.0). */
    @Column(name = "percent_male")
    private BigDecimal percentMale;

    /** Female appearance ratio (0.0 - 1.0). */
    @Column(name = "percent_female")
    private BigDecimal percentFemale;

    /** First egg group (e.g., Monster). */
    @Column(name = "egg_group1")
    private String eggGroup1;

    /** Second egg group (nullable). */
    @Column(name = "egg_group2")
    private String eggGroup2;

    /** URL pointing to the Pokémon image/sprite. */
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    // Constructors
    public Pokemon() {}

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNdex() {
        return ndex;
    }

    public void setNdex(Integer ndex) {
        this.ndex = ndex;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    public String getDex1() {
        return dex1;
    }

    public void setDex1(String dex1) {
        this.dex1 = dex1;
    }

    public String getDex2() {
        return dex2;
    }

    public void setDex2(String dex2) {
        this.dex2 = dex2;
    }

    public Type getType1() {
        return type1;
    }

    public void setType1(Type type1) {
        this.type1 = type1;
    }

    public Type getType2() {
        return type2;
    }

    public void setType2(Type type2) {
        this.type2 = type2;
    }

    public Ability getAbility1() {
        return ability1;
    }

    public void setAbility1(Ability ability1) {
        this.ability1 = ability1;
    }

    public Ability getAbility2() {
        return ability2;
    }

    public void setAbility2(Ability ability2) {
        this.ability2 = ability2;
    }

    public Ability getHiddenAbility() {
        return hiddenAbility;
    }

    public void setHiddenAbility(Ability hiddenAbility) {
        this.hiddenAbility = hiddenAbility;
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

    public int getSpattack() {
        return spattack;
    }

    public void setSpattack(int spattack) {
        this.spattack = spattack;
    }

    public int getSpdefense() {
        return spdefense;
    }

    public void setSpdefense(int spdefense) {
        this.spdefense = spdefense;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public String getPokemonClass() {
        return pokemonClass;
    }

    public void setPokemonClass(String pokemonClass) {
        this.pokemonClass = pokemonClass;
    }

    public BigDecimal getPercentMale() {
        return percentMale;
    }

    public void setPercentMale(BigDecimal percentMale) {
        this.percentMale = percentMale;
    }

    public BigDecimal getPercentFemale() {
        return percentFemale;
    }

    public void setPercentFemale(BigDecimal percentFemale) {
        this.percentFemale = percentFemale;
    }

    public String getEggGroup1() {
        return eggGroup1;
    }

    public void setEggGroup1(String eggGroup1) {
        this.eggGroup1 = eggGroup1;
    }

    public String getEggGroup2() {
        return eggGroup2;
    }

    public void setEggGroup2(String eggGroup2) {
        this.eggGroup2 = eggGroup2;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // equals & hashCode based on id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pokemon)) return false;
        Pokemon pokemon = (Pokemon) o;
        return Objects.equals(id, pokemon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
