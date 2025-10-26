package service;

import com.pokedexsocial.backend.dto.UpdateUserRequest;
import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.exception.InvalidUserOperationException;
import com.pokedexsocial.backend.exception.UserNotFoundException;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import com.pokedexsocial.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @AfterEach
    void clearContext() {
        AuthContext.clear();
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setUsername("ash");
        user.setEmail("ash@example.com");
        user.setFirstName("Ash");
        user.setLastName("Ketchum");
        user.setPokecoin(100L);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setPassword("encodedOldPass");
        return user;
    }

    // ==================== getUserInfo ====================

    @Test
    void getUserInfo_ShouldReturnInfo_WhenAuthorizedAndExists() throws Exception {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        User user = createUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserInfoDTO result = userService.getUserInfo(1);

        assertThat(result.getUsername()).isEqualTo("ash");
        verify(userRepository).findById(1);
    }

    @Test
    void getUserInfo_ShouldThrow_WhenUserNotFound() {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(1))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getUserInfo_ShouldThrow_WhenAccessDenied() {
        AuthContext.setCurrentUser(new AuthenticatedUser(2, "misty", "USER"));

        assertThatThrownBy(() -> userService.getUserInfo(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ==================== deleteUser ====================

    @Test
    void deleteUser_ShouldDelete_WhenAdmin() throws Exception {
        AuthContext.setCurrentUser(new AuthenticatedUser(99, "admin", "ADMIN"));
        User user = createUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUnauthorized() {
        AuthContext.setCurrentUser(new AuthenticatedUser(2, "misty", "USER"));
        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserNotFound() {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ==================== updateUser ====================

    @Test
    void updateUser_ShouldUpdateFields_WhenAuthorized() throws Exception {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        User user = createUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Ashura");
        request.setEmail("ash.new@example.com");

        UserInfoDTO result = userService.updateUser(1, request);

        assertThat(result.getFirstName()).isEqualTo("Ashura");
        assertThat(result.getEmail()).isEqualTo("ash.new@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ShouldChangePassword_WhenOldMatches() throws Exception {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        User user = createUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        userService.updateUser(1, req);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedNew");
    }

    @Test
    void updateUser_ShouldThrow_WhenOldPasswordIncorrect() {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        User user = createUser();
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedOldPass")).thenReturn(false);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setOldPassword("wrong");
        req.setNewPassword("new");

        assertThatThrownBy(() -> userService.updateUser(1, req))
                .isInstanceOf(InvalidUserOperationException.class);
    }

    @Test
    void updateUser_ShouldThrow_WhenUnauthorized() {
        AuthContext.setCurrentUser(new AuthenticatedUser(2, "misty", "USER"));
        UpdateUserRequest req = new UpdateUserRequest();

        assertThatThrownBy(() -> userService.updateUser(1, req))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateUser_ShouldThrow_WhenUserNotFound() {
        AuthContext.setCurrentUser(new AuthenticatedUser(1, "ash", "USER"));
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UpdateUserRequest req = new UpdateUserRequest();

        assertThatThrownBy(() -> userService.updateUser(1, req))
                .isInstanceOf(UserNotFoundException.class);
    }
}
