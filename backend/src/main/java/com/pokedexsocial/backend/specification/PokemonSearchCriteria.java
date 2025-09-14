package com.pokedexsocial.backend.specification;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Criteria object for filtering Pokémon in search queries.
 * Supports text search, type filtering, ability, ndex range,
 * and height/weight ranges.
 */
public class PokemonSearchCriteria {

    /** Species substring (case-insensitive). */
    private String q;

    /** Up to 2 type IDs can be selected. */
    @Size(max = 2, message = "Puoi selezionare al massimo 2 tipi")
    private List<Integer> typeIds;

    /** Minimum National Dex number (inclusive). */
    @Min(value = 1, message = "ndexFrom deve essere >= 1")
    private Integer ndexFrom;

    /** Maximum National Dex number (inclusive). */
    @Min(value = 1, message = "ndexTo deve essere >= 1")
    private Integer ndexTo;

    /** Ability ID filter. */
    @Min(value = 1, message = "abilityId deve essere >= 1")
    private Integer abilityId;

    /** Minimum height (inclusive). */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal heightFrom;

    /** Maximum height (inclusive). */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal heightTo;

    /** Minimum weight (inclusive). */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal weightFrom;

    /** Maximum weight (inclusive). */
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal weightTo;

    // getters & setters
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public List<Integer> getTypeIds() {
        return typeIds;
    }

    public void setTypeIds(List<Integer> typeIds) {
        this.typeIds = typeIds;
    }

    public Integer getNdexFrom() {
        return ndexFrom;
    }

    public void setNdexFrom(Integer ndexFrom) {
        this.ndexFrom = ndexFrom;
    }

    public Integer getNdexTo() {
        return ndexTo;
    }

    public void setNdexTo(Integer ndexTo) {
        this.ndexTo = ndexTo;
    }

    public Integer getAbilityId() {
        return abilityId;
    }

    public void setAbilityId(Integer abilityId) {
        this.abilityId = abilityId;
    }

    public BigDecimal getHeightFrom() {
        return heightFrom;
    }

    public void setHeightFrom(BigDecimal heightFrom) {
        this.heightFrom = heightFrom;
    }

    public BigDecimal getHeightTo() {
        return heightTo;
    }

    public void setHeightTo(BigDecimal heightTo) {
        this.heightTo = heightTo;
    }

    public BigDecimal getWeightFrom() {
        return weightFrom;
    }

    public void setWeightFrom(BigDecimal weightFrom) {
        this.weightFrom = weightFrom;
    }

    public BigDecimal getWeightTo() {
        return weightTo;
    }

    public void setWeightTo(BigDecimal weightTo) {
        this.weightTo = weightTo;
    }
}
