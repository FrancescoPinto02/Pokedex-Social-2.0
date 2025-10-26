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
        registrationRequest = new RegistrationRequest(
                "test@example.com",
                "testUser",
                "Password@123",
                "Mario",
                "Rossi",
                LocalDate.of(2000, 1, 1)
        );

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password@123");

        user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        user.setUsername("testUser");
        user.setPassword("encodedPassword");
        user.setRole("USER");
    }

    // ---------- REGISTER ----------

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyUsed() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(EmailAlreadyUsedException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyUsed() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registrationRequest))
                .isInstanceOf(UsernameAlreadyUsedException.class)
                .hasMessageContaining("Username already in use");

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByUsername("testUser");
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_ShouldSaveUserAndReturnAuthResponse() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1);
            return u;
        });
        when(jwtUtil.generateToken(1, "testUser", "USER")).thenReturn("mockedToken");

        AuthResponse response = authService.register(registrationRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockedToken");
        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getUsername()).isEqualTo("testUser");

        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(1, "testUser", "USER");
    }

    // ---------- LOGIN ----------

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_ShouldThrowException_WhenPasswordInvalid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(1, "testUser", "USER")).thenReturn("jwtToken");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getUserId()).isEqualTo(1);
        assertThat(response.getUsername()).isEqualTo("testUser");

        verify(jwtUtil).generateToken(1, "testUser", "USER");
    }
}
