package com.pokedexsocial.backend.dto;

import com.pokedexsocial.backend.model.User;

import java.time.LocalDate;

public class UserInfoDTO {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Long pokecoins;
    private LocalDate birthDate;

    public UserInfoDTO() {
    }

    public UserInfoDTO(Integer id, String username, String email, String firstName,
                       String lastName, Long pokeCoins, LocalDate birthDate) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.pokecoins = pokeCoins;
        this.birthDate = birthDate;
    }

    // ===== METODO STATICO fromEntity =====
    public static UserInfoDTO fromEntity(User user) {
        return new UserInfoDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPokecoin(),
                user.getBirthDate()
        );
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getPokecoins() {
        return pokecoins;
    }

    public void setPokecoins(Long pokecoins) {
        this.pokecoins = pokecoins;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
