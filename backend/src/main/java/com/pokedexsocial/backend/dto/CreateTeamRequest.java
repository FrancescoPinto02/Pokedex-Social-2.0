package com.pokedexsocial.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must be at most 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    @NotBlank(message = "Visibility is required")
    private String visibility; // "PUBLIC" or "PRIVATE"

    @NotEmpty(message = "A team must contain at least 1 Pokémon")
    @Size(max = 6, message = "A team can contain at most 6 Pokémon")
    @Valid
    private List<MemberDto> members;

    // --- GETTER & SETTER ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public List<MemberDto> getMembers() { return members; }
    public void setMembers(List<MemberDto> members) { this.members = members; }

    // --- Inner DTO per i membri ---
    public static class MemberDto {
        @NotNull(message = "Pokemon ID is required")
        private Integer pokemonId;

        @NotNull(message = "Slot is required")
        @Min(value = 1, message = "Slot must be between 1 and 6")
        @Max(value = 6, message = "Slot must be between 1 and 6")
        private Integer slot;

        public Integer getPokemonId() { return pokemonId; }
        public void setPokemonId(Integer pokemonId) { this.pokemonId = pokemonId; }

        public Integer getSlot() { return slot; }
        public void setSlot(Integer slot) { this.slot = slot; }
    }
}
