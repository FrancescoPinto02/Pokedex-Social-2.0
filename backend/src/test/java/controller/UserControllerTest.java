package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedexsocial.backend.controller.UserController;
import com.pokedexsocial.backend.dto.UpdateUserRequest;
import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
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

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(controllers = UserController.class)
@ContextConfiguration(classes = {UserController.class, UserControllerTest.TestConfig.class})
class UserControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserInfoDTO mockUser;

    @BeforeEach
    void setup() {
        mockUser = new UserInfoDTO(
                1,
                "ash",
                "ash@example.com",
                "Ash",
                "Ketchum",
                100L,
                LocalDate.of(2000, 1, 1)
        );
    }

    @AfterEach
    void tearDown() {
        reset(userService);
    }

    // ==================== GET /user/{id} ====================

    @Test
    void getUserById_ShouldReturnOkAndUserInfo() throws Exception {
        when(userService.getUserInfo(1)).thenReturn(mockUser);

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.email").value("ash@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ash"))
                .andExpect(jsonPath("$.lastName").value("Ketchum"));

        verify(userService).getUserInfo(1);
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        Mockito.doAnswer(inv -> { throw new AccessDeniedException("Forbidden"); })
                .when(userService).getUserInfo(1);

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /user/{id} ====================

    @Test
    void deleteUser_ShouldReturnNoContent_WhenAuthorized() throws Exception {
        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1);
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        Mockito.doThrow(new AccessDeniedException("Forbidden"))
                .when(userService).deleteUser(1);

        mockMvc.perform(delete("/user/1"))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /user/{id} ====================

    @Test
    void updateUser_ShouldReturnOkAndUpdatedUserInfo() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Ashura");
        req.setLastName("Ketchum");
        req.setEmail("ash.new@example.com");

        UserInfoDTO updated = new UserInfoDTO(
                1,
                "ash",
                "ash.new@example.com",
                "Ashura",
                "Ketchum",
                100L,
                LocalDate.of(2000, 1, 1)
        );

        when(userService.updateUser(eq(1), any(UpdateUserRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ash.new@example.com"))
                .andExpect(jsonPath("$.firstName").value("Ashura"));

        verify(userService).updateUser(eq(1), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnForbidden_WhenAccessDenied() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFirstName("Ashura");

        Mockito.doThrow(new AccessDeniedException("Forbidden"))
                .when(userService).updateUser(eq(1), any(UpdateUserRequest.class));

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenRequestBodyInvalid() throws Exception {
        // corpo JSON vuoto → @Valid fallisce se i campi sono obbligatori
        String invalidJson = "{}";

        mockMvc.perform(put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
