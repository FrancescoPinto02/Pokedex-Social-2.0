package com.pokedexsocial.backend.dto;

public class AuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private Integer userId;
    private String username;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType, Integer userId, String username) {
        this.token = token;
        this.tokenType = tokenType;
        this.userId = userId;
        this.username = username;
    }

    public AuthResponse(String token, int userId, String username) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
