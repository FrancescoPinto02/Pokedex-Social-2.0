package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.AuthResponse;
import com.pokedexsocial.backend.dto.LoginRequest;
import com.pokedexsocial.backend.dto.RegistrationRequest;
import com.pokedexsocial.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller responsible for authentication-related endpoints.
 *
 * <p>Endpoints:</p>
 * <ul>
 *     <li>POST /auth/register - register a new user</li>
 *     <li>POST /auth/login - login an existing user</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs an {@code AuthController} with the specified authentication service.
     *
     * @param authService the service responsible for authentication and registration
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user account.
     * <p>
     * Accepts a {@link RegistrationRequest} containing user details, validates the input,
     * and returns an {@link AuthResponse} containing a JWT token and basic user information.
     * </p>
     *
     * @param request the registration request
     * @return a {@link ResponseEntity} with status {@link HttpStatus#CREATED} containing the auth response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegistrationRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticates an existing user.
     * <p>
     * Accepts a {@link LoginRequest} containing email and password. Returns an {@link AuthResponse}
     * with a JWT token and basic user information if the credentials are valid.
     * </p>
     *
     * @param request the login request
     * @return a {@link ResponseEntity} with status {@link HttpStatus#OK} containing the auth response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
