package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.AuthResponse;
import com.pokedexsocial.backend.dto.LoginRequest;
import com.pokedexsocial.backend.dto.RegistrationRequest;
import com.pokedexsocial.backend.exception.EmailAlreadyUsedException;
import com.pokedexsocial.backend.exception.InvalidCredentialsException;
import com.pokedexsocial.backend.exception.UsernameAlreadyUsedException;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for user authentication and registration.
 * <p>
 * This service handles the creation of new user accounts and the authentication
 * of existing users. It generates JWT tokens for successful registrations and logins,
 * ensuring secure access to protected resources.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Constructs an {@code AuthService} with the specified dependencies.
     *
     * @param userRepository  the repository used to access user data
     * @param passwordEncoder the encoder used to hash user passwords
     * @param jwtUtil the utility class for generating and validating JWT tokens
     */
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user account.
     * <p>
     * Checks for duplicate email or username and hashes the password before saving the user.
     * Returns an {@link AuthResponse} containing a JWT token and basic user information.
     * </p>
     *
     * @param request the registration request containing user details
     * @return an {@code AuthResponse} with the generated JWT token, user ID, and username
     * @throws EmailAlreadyUsedException    if the email is already registered
     * @throws UsernameAlreadyUsedException if the username is already taken
     */
    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyUsedException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyUsedException("Username already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBirthDate(request.getBirthDate());
        user.setPokecoin(0L);
        user.setRole("USER");

        User saved = userRepository.save((User) user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }

    /**
     * Authenticates an existing user and generates a JWT token.
     * <p>
     * Validates the email and password provided in the login request.
     * Returns an {@link AuthResponse} containing a JWT token and basic user information.
     * </p>
     *
     * @param request the login request containing email and password
     * @return an {@code AuthResponse} with the generated JWT token, user ID, and username
     * @throws InvalidCredentialsException if the email or password is incorrect
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername());
    }
}
