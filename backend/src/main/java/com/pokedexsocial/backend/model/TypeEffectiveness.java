package com.pokedexsocial.backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Represents the damage multiplier when one type attacks another.
 * Example: Fire -> Grass = 2.0, Fire -> Water = 0.5.
 */
@Entity
@Table(name = "type_effectiveness")
public class TypeEffectiveness {

    @EmbeddedId
    private TypeEffectivenessId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("attackerTypeId")
    @JoinColumn(name = "attacker_type_id", nullable = false)
    private Type attackerType;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("defenderTypeId")
    @JoinColumn(name = "defender_type_id", nullable = false)
    private Type defenderType;

    @Column(nullable = false)
    private BigDecimal multiplier;

    // Costruttori
    public TypeEffectiveness() {}

    public TypeEffectiveness(Type attackerType, Type defenderType, BigDecimal multiplier) {
        this.attackerType = attackerType;
        this.defenderType = defenderType;
        this.multiplier = multiplier;
        this.id = new TypeEffectivenessId(attackerType.getId(), defenderType.getId());
    }

    // Getter e Setter
    public TypeEffectivenessId getId() {
        return id;
    }

    public void setId(TypeEffectivenessId id) {
        this.id = id;
    }

    public Type getAttackerType() {
        return attackerType;
    }

    public void setAttackerType(Type attackerType) {
        this.attackerType = attackerType;
    }

    public Type getDefenderType() {
        return defenderType;
    }

    public void setDefenderType(Type defenderType) {
        this.defenderType = defenderType;
    }

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }
}
