package service;

import com.pokedexsocial.backend.exception.TeamNotFoundException;
import com.pokedexsocial.backend.model.Team;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.TeamRepository;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import com.pokedexsocial.backend.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository = mock(TeamRepository.class);
        userRepository = mock(UserRepository.class);
        teamService = new TeamService(teamRepository, null, userRepository); // pokemonRepo non serve per delete
    }

    @Test
    void ownerCanDeleteTeam() throws AccessDeniedException {
        // Arrange
        AuthenticatedUser current = new AuthenticatedUser(1, "ash", "USER");
        AuthContext.setCurrentUser(current);

        User user = new User();
        user.setId(1);

        Team team = new Team();
        team.setId(10);
        team.setUser(user);

        when(teamRepository.findById(10)).thenReturn(Optional.of(team));

        // Act
        teamService.deleteTeam(10);

        // Assert
        verify(teamRepository, times(1)).delete(team);
    }

    @Test
    void adminCanDeleteAnyTeam() throws AccessDeniedException {
        // Arrange
        AuthenticatedUser current = new AuthenticatedUser(99, "oak", "ADMIN");
        AuthContext.setCurrentUser(current);

        User owner = new User();
        owner.setId(1);

        Team team = new Team();
        team.setId(11);
        team.setUser(owner);

        when(teamRepository.findById(11)).thenReturn(Optional.of(team));

        // Act
        teamService.deleteTeam(11);

        // Assert
        verify(teamRepository, times(1)).delete(team);
    }

    @Test
    void nonOwnerNonAdminCannotDelete() {
        // Arrange
        AuthenticatedUser current = new AuthenticatedUser(2, "misty", "USER");
        AuthContext.setCurrentUser(current);

        User owner = new User();
        owner.setId(1);

        Team team = new Team();
        team.setId(12);
        team.setUser(owner);

        when(teamRepository.findById(12)).thenReturn(Optional.of(team));

        // Act + Assert
        assertThrows(AccessDeniedException.class, () -> teamService.deleteTeam(12));
        verify(teamRepository, never()).delete(any());
    }

    @Test
    void teamNotFoundThrowsException() {
        // Arrange
        AuthenticatedUser current = new AuthenticatedUser(1, "ash", "USER");
        AuthContext.setCurrentUser(current);

        when(teamRepository.findById(99)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(TeamNotFoundException.class, () -> teamService.deleteTeam(99));
        verify(teamRepository, never()).delete(any());
    }
}