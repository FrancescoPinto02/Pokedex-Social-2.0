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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private MockedStatic<AuthContext> authContextMock;

    private final AuthenticatedUser regularUser = new AuthenticatedUser(1, "username","USER");
    private final AuthenticatedUser adminUser = new AuthenticatedUser(99, "AdminUsername", "ADMIN");

    @BeforeEach
    void setUp() {
        authContextMock = Mockito.mockStatic(AuthContext.class);
    }

    @AfterEach
    void tearDown() {
        authContextMock.close();
    }

    // region getUserInfo

    /** Should return UserInfoDTO when the current user is owner. */
    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenUserIsOwner() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("owner@example.com");
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.getUserInfo(1);

            assertThat(result).isSameAs(dto);
            dtoMock.verify(() -> UserInfoDTO.fromEntity(user));
        }
    }

    /** Should return UserInfoDTO when current user is ADMIN accessing another user. */
    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenUserIsAdmin() throws Exception {
        User user = new User();
        user.setId(5);
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(adminUser);
        when(userRepository.findById(5)).thenReturn(Optional.of(user));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.getUserInfo(5);

            assertThat(result).isEqualTo(dto);
        }
    }

    /** Should throw AccessDeniedException when non-admin tries to access another user's data. */
    @Test
    void getUserInfo_ShouldThrowAccessDenied_WhenUserAccessesAnotherUser() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);

        assertThatThrownBy(() -> userService.getUserInfo(2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot access");
    }

    /** Should throw UserNotFoundException when user not found in repository. */
    @Test
    void getUserInfo_ShouldThrowUserNotFound_WhenRepositoryEmpty() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(adminUser);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(1))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    // endregion

    // region deleteUser

    /** Should delete user when current user is owner. */
    @Test
    void deleteUser_ShouldDelete_WhenUserIsOwner() throws Exception {
        User user = new User();
        user.setId(1);
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository).delete(user);
    }

    /** Should delete user when current user is ADMIN. */
    @Test
    void deleteUser_ShouldDelete_WhenUserIsAdmin() throws Exception {
        User user = new User();
        user.setId(2);
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(adminUser);
        when(userRepository.findById(2)).thenReturn(Optional.of(user));

        userService.deleteUser(2);

        verify(userRepository).delete(user);
    }

    /** Should throw AccessDeniedException when unauthorized user attempts delete. */
    @Test
    void deleteUser_ShouldThrowAccessDenied_WhenUnauthorized() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);

        assertThatThrownBy(() -> userService.deleteUser(3))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot delete");
        verify(userRepository, never()).delete(any());
    }

    /** Should throw UserNotFoundException when user not found during delete. */
    @Test
    void deleteUser_ShouldThrowUserNotFound_WhenNotExists() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(adminUser);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(UserNotFoundException.class);
    }

    // endregion

    // region updateUser

    /** Should update basic fields when provided. */
    @Test
    void updateUser_ShouldUpdateBasicFields_WhenFieldsProvided() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPassword("hashed");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@doe.com");

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.updateUser(1, req);

            assertThat(result).isSameAs(dto);
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getEmail()).isEqualTo("john@doe.com");
        }
    }



    /** Should not attempt password change when only one password field is provided. */
    @Test
    void updateUser_ShouldSkipPasswordChange_WhenIncompletePasswordFields() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPassword("encodedOld");

        // Only old password provided, missing newPassword
        UpdateUserRequest req = new UpdateUserRequest();
        req.setOldPassword("old");

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.updateUser(1, req);

            assertThat(result).isSameAs(dto);
            // Password should remain unchanged
            assertThat(user.getPassword()).isEqualTo("encodedOld");

            // passwordEncoder should not be used
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    /** Should change password when oldPassword matches. */
    @Test
    void updateUser_ShouldChangePassword_WhenOldPasswordMatches() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPassword("encodedOld");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setOldPassword("old");
        req.setNewPassword("new");

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("new")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenReturn(user);

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.updateUser(1, req);

            assertThat(result).isEqualTo(dto);
            assertThat(user.getPassword()).isEqualTo("encodedNew");
        }
    }

    /** Should throw InvalidUserOperationException when old password is incorrect. */
    @Test
    void updateUser_ShouldThrowInvalidOperation_WhenOldPasswordIncorrect() {
        User user = new User();
        user.setId(1);
        user.setPassword("encodedOld");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setOldPassword("wrong");
        req.setNewPassword("new");

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(1, req))
                .isInstanceOf(InvalidUserOperationException.class)
                .hasMessageContaining("Old password is incorrect");
    }

    /** Should throw AccessDeniedException when unauthorized user attempts update. */
    @Test
    void updateUser_ShouldThrowAccessDenied_WhenUnauthorized() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);

        assertThatThrownBy(() -> userService.updateUser(5, new UpdateUserRequest()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot update");
    }

    /** Should throw UserNotFoundException when repository returns empty. */
    @Test
    void updateUser_ShouldThrowUserNotFound_WhenRepositoryEmpty() {
        authContextMock.when(AuthContext::getCurrentUser).thenReturn(adminUser);
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(1, new UpdateUserRequest()))
                .isInstanceOf(UserNotFoundException.class);
    }

    /** Should not update any basic fields when request fields are null. */
    @Test
    void updateUser_ShouldNotChangeFields_WhenRequestFieldsAreNull() throws Exception {
        User user = new User();
        user.setId(1);
        user.setFirstName("Alice");
        user.setLastName("Wonderland");
        user.setEmail("alice@wonder.com");
        user.setPassword("encodedOld");

        UpdateUserRequest req = new UpdateUserRequest(); // tutti i campi null

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.updateUser(1, req);

            assertThat(result).isSameAs(dto);

            // 🔥 qui uccidiamo i mutanti: verifichiamo che i valori non cambino
            assertThat(user.getFirstName()).isEqualTo("Alice");
            assertThat(user.getLastName()).isEqualTo("Wonderland");
            assertThat(user.getEmail()).isEqualTo("alice@wonder.com");
        }
    }

    @Test
    void updateUser_ShouldSkipPasswordChange_WhenOnlyNewPasswordProvided() throws Exception {
        User user = new User();
        user.setId(1);
        user.setPassword("encodedOld");

        // Solo newPassword valorizzata
        UpdateUserRequest req = new UpdateUserRequest();
        req.setNewPassword("newOnly");

        authContextMock.when(AuthContext::getCurrentUser).thenReturn(regularUser);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        try (MockedStatic<UserInfoDTO> dtoMock = Mockito.mockStatic(UserInfoDTO.class)) {
            UserInfoDTO dto = new UserInfoDTO();
            dtoMock.when(() -> UserInfoDTO.fromEntity(user)).thenReturn(dto);

            UserInfoDTO result = userService.updateUser(1, req);

            assertThat(result).isSameAs(dto);
            assertThat(user.getPassword()).isEqualTo("encodedOld");

            // Verifica che nessuna chiamata al passwordEncoder sia avvenuta
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    // endregion
}