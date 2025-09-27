package com.pokedexsocial.backend.dto;

import java.util.Map;
import java.util.List;

public class PokemonDto {
    private Integer id;
    private Integer ndex;
    private String species;
    private String forme;
    private String dex1;
    private String dex2;

    private Integer hp;
    private Integer attack;
    private Integer defense;
    private Integer spattack;
    private Integer spdefense;
    private Integer speed;
    private Integer total;

    private Double weight;
    private Double height;

    private String pokemonClass;
    private Double percentMale;
    private Double percentFemale;

    private String eggGroup1;
    private String eggGroup2;

    private String imageUrl;

    private TypeDto type1;
    private TypeDto type2;

    private AbilityDto ability1;
    private AbilityDto ability2;
    private AbilityDto hiddenAbility;

    private Map<String, Double> weaknesses;
    private Map<String, Double> resistances;
    private Map<String, Double> neutral;

    // Costruttore vuoto
    public PokemonDto() {}

    // Getter & Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNdex() { return ndex; }
    public void setNdex(Integer ndex) { this.ndex = ndex; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getForme() { return forme; }
    public void setForme(String forme) { this.forme = forme; }

    public String getDex1() { return dex1; }
    public void setDex1(String dex1) { this.dex1 = dex1; }

    public String getDex2() { return dex2; }
    public void setDex2(String dex2) { this.dex2 = dex2; }

    public Integer getHp() { return hp; }
    public void setHp(Integer hp) { this.hp = hp; }

    public Integer getAttack() { return attack; }
    public void setAttack(Integer attack) { this.attack = attack; }

    public Integer getDefense() { return defense; }
    public void setDefense(Integer defense) { this.defense = defense; }

    public Integer getSpattack() { return spattack; }
    public void setSpattack(Integer spattack) { this.spattack = spattack; }

    public Integer getSpdefense() { return spdefense; }
    public void setSpdefense(Integer spdefense) { this.spdefense = spdefense; }

    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public String getPokemonClass() { return pokemonClass; }
    public void setPokemonClass(String pokemonClass) { this.pokemonClass = pokemonClass; }

    public Double getPercentMale() { return percentMale; }
    public void setPercentMale(Double percentMale) { this.percentMale = percentMale; }

    public Double getPercentFemale() { return percentFemale; }
    public void setPercentFemale(Double percentFemale) { this.percentFemale = percentFemale; }

    public String getEggGroup1() { return eggGroup1; }
    public void setEggGroup1(String eggGroup1) { this.eggGroup1 = eggGroup1; }

    public String getEggGroup2() { return eggGroup2; }
    public void setEggGroup2(String eggGroup2) { this.eggGroup2 = eggGroup2; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public TypeDto getType1() { return type1; }
    public void setType1(TypeDto type1) { this.type1 = type1; }

    public TypeDto getType2() { return type2; }
    public void setType2(TypeDto type2) { this.type2 = type2; }

    public AbilityDto getAbility1() { return ability1; }
    public void setAbility1(AbilityDto ability1) { this.ability1 = ability1; }

    public AbilityDto getAbility2() { return ability2; }
    public void setAbility2(AbilityDto ability2) { this.ability2 = ability2; }

    public AbilityDto getHiddenAbility() { return hiddenAbility; }
    public void setHiddenAbility(AbilityDto hiddenAbility) { this.hiddenAbility = hiddenAbility; }

    public Map<String, Double> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(Map<String, Double> weaknesses) { this.weaknesses = weaknesses; }

    public Map<String, Double> getResistances() { return resistances; }
    public void setResistances(Map<String, Double> resistances) { this.resistances = resistances; }

    public Map<String, Double> getNeutral() { return neutral; }
    public void setNeutral(Map<String, Double> neutral) { this.neutral = neutral; }
}
