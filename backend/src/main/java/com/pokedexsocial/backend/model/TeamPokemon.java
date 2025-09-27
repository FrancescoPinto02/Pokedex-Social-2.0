package com.pokedexsocial.backend.model;

import jakarta.persistence.*;

/**
 * Represents the association between a {@link Team} and a {@link Pokemon}.
 *
 * <p>This entity models the Pokémon assigned to a team in a specific slot
 * (from 1 to 6). Each slot uniquely identifies a position within a team,
 * ensuring that no two Pokémon occupy the same slot.
 *
 * <p>The relationship is bidirectional: the team owns the collection of
 * members, while each {@code TeamPokemon} points to both the team and the
 * assigned Pokémon.
 *
 * <p>The primary key is composed of the team identifier and the slot number,
 * represented by {@link TeamPokemonId}.
 */
@Entity
@Table(name = "team_pokemon")
public class TeamPokemon {

    @EmbeddedId
    private TeamPokemonId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    public TeamPokemon() {}

    public TeamPokemon(Team team, Pokemon pokemon, Integer slot) {
        this.team = team;
        this.pokemon = pokemon;
        this.id = new TeamPokemonId(team.getId(), slot);
    }

    public TeamPokemonId getId() { return id; }
    public void setId(TeamPokemonId id) { this.id = id; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public Pokemon getPokemon() { return pokemon; }
    public void setPokemon(Pokemon pokemon) { this.pokemon = pokemon; }

    public Integer getSlot() { return id != null ? id.getSlot() : null; }
}