package service;

import com.pokedexsocial.backend.dto.AuthResponse;
import com.pokedexsocial.backend.dto.LoginRequest;
import com.pokedexsocial.backend.dto.RegistrationRequest;
import com.pokedexsocial.backend.exception.EmailAlreadyUsedException;
import com.pokedexsocial.backend.exception.InvalidCredentialsException;
import com.pokedexsocial.backend.exception.UsernameAlreadyUsedException;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.JwtUtil;
import com.pokedexsocial.backend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegistrationRequest registrationRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registrationRequest = new RegistrationRequest();
        registrationRequest.setEmail("ash@pokedex.com");
        registrationRequest.setUsername("ashKetchum");
        registrationRequest.setPassword("pikachu123");
        registrationRequest.setFirstName("Ash");
        registrationRequest.setLastName("Ketchum");
        registrationRequest.setBirthDate(LocalDate.of(2000, 1, 1));

        loginRequest = new LoginRequest();
        loginRequest.setEmail("ash@pokedex.com");
        loginRequest.setPassword("pikachu123");

        user = new User();
        user.setId(1);
        user.setEmail("ash@pokedex.com");
        user.setUsername("ashKetchum");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }


    // ####################################### register(registrationRequest) ###########################################
    /** Should throw EmailAlreadyUsedException when email is already in use. */
    @Test
    void register_ShouldThrowException_WhenEmailAlreadyUsed() {
        when(userRepository.existsByEmail("ash@pokedex.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository).existsByEmail("ash@pokedex.com");
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any());
    }

    /** Should throw UsernameAlreadyUsedException when username is already in use. */
    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyUsed() {
        when(userRepository.existsByEmail("ash@pokedex.com")).thenReturn(false);
        when(userRepository.existsByUsername("ashKetchum")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(UsernameAlreadyUsedException.class)
                .hasMessageContaining("Username already in use");

        verify(userRepository).existsByEmail("ash@pokedex.com");
        verify(userRepository).existsByUsername("ashKetchum");
        verify(userRepository, never()).save(any());
    }

    /** Should register user and return AuthResponse when input is valid. */
    @Test
    void register_ShouldReturnAuthResponse_WhenDataIsValid() {
        when(userRepository.existsByEmail("ash@pokedex.com")).thenReturn(false);
        when(userRepository.existsByUsername("ashKetchum")).thenReturn(false);
        when(passwordEncoder.encode("pikachu123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(42);
            return saved;
        });
        when(jwtUtil.generateToken(42, "ashKetchum", "USER")).thenReturn("jwtToken");

        AuthResponse response = authService.register(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getUserId()).isEqualTo(42);
        assertThat(response.getUsername()).isEqualTo("ashKetchum");

        verify(passwordEncoder).encode("pikachu123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(42, "ashKetchum", "USER");
    }
    // #################################################################################################################


    // ####################################### login(loginRequest) #####################################################
    /** Should throw InvalidCredentialsException when email is not found. */
    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {
        when(userRepository.findByEmail("ash@pokedex.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("ash@pokedex.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyInt(), anyString(), anyString());
    }

    /** Should throw InvalidCredentialsException when password does not match. */
    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByEmail("ash@pokedex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pikachu123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");

        verify(userRepository).findByEmail("ash@pokedex.com");
        verify(passwordEncoder).matches("pikachu123", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyInt(), anyString(), anyString());
    }

    /** Should return AuthResponse when credentials are valid. */
    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(userRepository.findByEmail("ash@pokedex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pikachu123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1, "ashKetchum", "USER")).thenReturn("jwtToken");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("ashKetchum");

        verify(userRepository).findByEmail("ash@pokedex.com");
        verify(passwordEncoder).matches("pikachu123", "encodedPassword");
        verify(jwtUtil).generateToken(1, "ashKetchum", "USER");
    }
}
// #####################################################################################################################
