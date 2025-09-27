package com.pokedexsocial.backend.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for the {@link TeamPokemon} entity.
 *
 * <p>Each entry is uniquely identified by:
 * <ul>
 *   <li>{@code teamId} – the identifier of the team the Pokémon belongs to.</li>
 *   <li>{@code slot} – the position of the Pokémon in the team (1 to 6).</li>
 * </ul>
 *
 * <p>This composite key ensures that each slot within a team is unique.
 * The same slot cannot contain more than one Pokémon.
 */
@Embeddable
public class TeamPokemonId implements Serializable {
    private Integer teamId;
    private Integer slot;

    public TeamPokemonId() {}

    public TeamPokemonId(Integer teamId, Integer slot) {
        this.teamId = teamId;
        this.slot = slot;
    }

    // getter & setter
    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getSlot() {
        return slot;
    }

    public void setSlot(Integer slot) {
        this.slot = slot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamPokemonId)) return false;
        TeamPokemonId that = (TeamPokemonId) o;
        return Objects.equals(teamId, that.teamId) &&
                Objects.equals(slot, that.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, slot);
    }
}
