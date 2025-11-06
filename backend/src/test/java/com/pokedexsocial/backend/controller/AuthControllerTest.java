package com.pokedexsocial.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexsocial.backend.dto.AuthResponse;
import com.pokedexsocial.backend.dto.LoginRequest;
import com.pokedexsocial.backend.dto.RegistrationRequest;
import com.pokedexsocial.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test for AuthController using modern @WebMvcTest approach.
 */
@WebMvcTest(controllers = AuthController.class)
@ContextConfiguration(classes = {AuthController.class, AuthControllerTest.TestConfig.class})
class AuthControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegistrationRequest validRegistrationRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setup() {
        validRegistrationRequest = new RegistrationRequest(
                "test@example.com",
                "testUser",
                "Password@123",
                "Mario",
                "Rossi",
                LocalDate.of(2000, 1, 1)
        );

        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("test@example.com");
        validLoginRequest.setPassword("Password@123");
    }

    @Test
    void register_ShouldReturnCreatedAndAuthResponse() throws Exception {
        AuthResponse mockResponse = new AuthResponse("mockToken", 1, "testUser");
        when(authService.register(any(RegistrationRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));

        verify(authService).register(any(RegistrationRequest.class));
    }

    @Test
    void login_ShouldReturnOkAndAuthResponse() throws Exception {
        AuthResponse mockResponse = new AuthResponse("jwtToken", 1, "testUser");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwtToken"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testUser"));

        verify(authService).login(any(LoginRequest.class));
    }

    // Email non valida → 400
    @Test
    void register_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        RegistrationRequest invalid = new RegistrationRequest(
                "invalid-email",
                "testUser",
                "Password@123",
                "Mario",
                "Rossi",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // Username troppo corto → 400
    @Test
    void register_ShouldReturnBadRequest_WhenUsernameTooShort() throws Exception {
        RegistrationRequest invalid = new RegistrationRequest(
                "test@example.com",
                "usr", // troppo corto
                "Password@123",
                "Mario",
                "Rossi",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // Password senza carattere speciale → 400
    @Test
    void register_ShouldReturnBadRequest_WhenPasswordInvalidFormat() throws Exception {
        RegistrationRequest invalid = new RegistrationRequest(
                "test@example.com",
                "validUser",
                "Password123", // manca il carattere speciale
                "Mario",
                "Rossi",
                LocalDate.of(2000, 1, 1)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
