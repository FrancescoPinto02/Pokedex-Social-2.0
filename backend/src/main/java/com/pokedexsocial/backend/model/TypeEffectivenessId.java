package com.pokedexsocial.backend.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TypeEffectivenessId implements Serializable {

    private Integer attackerTypeId;
    private Integer defenderTypeId;

    public TypeEffectivenessId() {}

    public TypeEffectivenessId(Integer attackerTypeId, Integer defenderTypeId) {
        this.attackerTypeId = attackerTypeId;
        this.defenderTypeId = defenderTypeId;
    }

    // Getter e Setter
    public Integer getAttackerTypeId() {
        return attackerTypeId;
    }

    public void setAttackerTypeId(Integer attackerTypeId) {
        this.attackerTypeId = attackerTypeId;
    }

    public Integer getDefenderTypeId() {
        return defenderTypeId;
    }

    public void setDefenderTypeId(Integer defenderTypeId) {
        this.defenderTypeId = defenderTypeId;
    }

    // equals & hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeEffectivenessId)) return false;
        TypeEffectivenessId that = (TypeEffectivenessId) o;
        return Objects.equals(attackerTypeId, that.attackerTypeId) &&
                Objects.equals(defenderTypeId, that.defenderTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attackerTypeId, defenderTypeId);
    }
}
