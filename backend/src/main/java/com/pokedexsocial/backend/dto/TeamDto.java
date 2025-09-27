package com.pokedexsocial.backend.dto;

import java.time.Instant;
import java.util.List;

public class TeamDto {
    private Integer id;
    private String name;
    private String description;
    private String visibility;
    private Instant createdAt;
    private Instant updatedAt;

    private UserSummaryDto user;
    private List<MemberDto> members;

    // --- Inner DTOs ---
    public static class UserSummaryDto {
        private Integer id;
        private String username;

        public UserSummaryDto(Integer id, String username) {
            this.id = id;
            this.username = username;
        }

        public Integer getId() { return id; }
        public String getUsername() { return username; }
    }

    public static class MemberDto {
        private Integer slot;
        private PokemonListDto pokemon;  // 👈 uso del nuovo DTO

        public MemberDto(Integer slot, PokemonListDto pokemon) {
            this.slot = slot;
            this.pokemon = pokemon;
        }

        public Integer getSlot() { return slot; }
        public PokemonListDto getPokemon() { return pokemon; }
    }

    // --- Getters & Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public UserSummaryDto getUser() { return user; }
    public void setUser(UserSummaryDto user) { this.user = user; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }
}
