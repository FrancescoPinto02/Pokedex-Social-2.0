package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.CreateTeamRequest;
import com.pokedexsocial.backend.exception.InvalidTeamOperationException;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Team;
import com.pokedexsocial.backend.dto.TeamDto;
import com.pokedexsocial.backend.model.TeamPokemon;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.PokemonRepository;
import com.pokedexsocial.backend.repository.TeamRepository;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import com.pokedexsocial.backend.service.TeamService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private PokemonRepository pokemonRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private TeamService teamService;

    private AuthenticatedUser authUser;
    private User dbUser;
    private CreateTeamRequest request;
    private Pokemon bulbasaur;

    @BeforeEach
    void setUp() {
        // Simulate authenticated user
        authUser = new AuthenticatedUser(1, "ash", "USER");
        AuthContext.setCurrentUser(authUser);

        // User from DB
        dbUser = new User();
        dbUser.setId(1);
        dbUser.setUsername("ash");

        // Pokémon stub
        bulbasaur = new Pokemon();
        bulbasaur.setId(1);
        bulbasaur.setSpecies("Bulbasaur");
        bulbasaur.setNdex(1);

        // Request with only 1 valid member
        CreateTeamRequest.MemberDto member = new CreateTeamRequest.MemberDto();
        member.setPokemonId(1);
        member.setSlot(1);

        request = new CreateTeamRequest();
        request.setName("Kanto Starters");
        request.setDescription("My starter squad");
        request.setVisibility("PUBLIC");
        request.setMembers(List.of(member));
    }

    @AfterEach
    void tearDown() {
        AuthContext.clear();
    }

    // -------------------------------------------------------------------------------------
    // Utente non autenticato → AccessDeniedException
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear(); // simulate no user logged in

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    // -------------------------------------------------------------------------------------
    // 2️⃣ Utente autenticato ma non trovato nel DB → InvalidTeamOperationException
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldThrowInvalidOperation_WhenAuthenticatedUserNotFoundInDb() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Authenticated user not found");
    }

    // -------------------------------------------------------------------------------------
    // 3️⃣ Visibility non valida → InvalidTeamOperationException
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldThrowInvalidOperation_WhenVisibilityInvalid() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));
        request.setVisibility("SECRET");

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Visibility must be PUBLIC or PRIVATE");
    }

    // -------------------------------------------------------------------------------------
    // 4️⃣ Slot duplicati → InvalidTeamOperationException
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldThrowInvalidOperation_WhenDuplicateSlotsExist() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));

        CreateTeamRequest.MemberDto m1 = new CreateTeamRequest.MemberDto();
        m1.setPokemonId(1);
        m1.setSlot(1);

        CreateTeamRequest.MemberDto m2 = new CreateTeamRequest.MemberDto();
        m2.setPokemonId(2);
        m2.setSlot(1); // duplicate slot

        request.setMembers(List.of(m1, m2));

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Duplicate slot 1 in team");
    }

    // -------------------------------------------------------------------------------------
    // 5️⃣ Pokémon non trovato → InvalidTeamOperationException
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldThrowInvalidOperation_WhenPokemonNotFound() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));
        when(pokemonRepository.findById(anyInt())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Pokemon not found");
    }

    // -------------------------------------------------------------------------------------
    // 6️⃣ Caso positivo → Team creato con successo
    // -------------------------------------------------------------------------------------
    @Test
    void createTeam_ShouldCreateTeamSuccessfully_WhenRequestValid() throws Exception {
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));
        when(pokemonRepository.findById(1)).thenReturn(Optional.of(bulbasaur));

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(99);
            t.setCreatedAt(java.time.Instant.now());
            t.setUpdatedAt(java.time.Instant.now());
            return t;
        });

        TeamDto result = teamService.createTeam(request);

        // ---- verifiche DTO per uccidere mutanti 297,299,300 ----
        assertThat(result.getDescription()).isEqualTo("My starter squad");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();

        // ---- verifica sul Team salvato (uccide mutanti 82) ----
        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        Team saved = captor.getValue();

        assertThat(saved.getDescription()).isEqualTo("My starter squad");
        assertThat(saved.getName()).isEqualTo("Kanto Starters");

        verify(teamRepository).save(any(Team.class));
    }

    /**
     * Should throw InvalidTeamOperationException when visibility is null (indirect test of validateVisibility)
     */
    @Test
    void createTeam_ShouldThrowInvalidOperation_WhenVisibilityIsNull() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));
        request.setVisibility(null); // forza il ramo "visibility == null"

        // when / then
        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Visibility is required");
    }

    @Test
    void createTeam_ShouldCreateTeamSuccessfully_WhenVisibilityPrivate() throws Exception {
        // authenticated user presente nel @BeforeEach
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));

        // pokemon esistente
        when(pokemonRepository.findById(1)).thenReturn(Optional.of(bulbasaur));

        // request con visibilità PRIVATE
        CreateTeamRequest.MemberDto member = new CreateTeamRequest.MemberDto();
        member.setPokemonId(1);
        member.setSlot(1);

        CreateTeamRequest privateReq = new CreateTeamRequest();
        privateReq.setName("Private Team");
        privateReq.setDescription("Team with PRIVATE visibility");
        privateReq.setVisibility("PRIVATE");
        privateReq.setMembers(List.of(member));

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(101);
            return t;
        });

        // when
        TeamDto result = teamService.createTeam(privateReq);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(101);
        assertThat(result.getVisibility()).isEqualTo("PRIVATE");
        assertThat(result.getMembers()).hasSize(1);
        verify(teamRepository).save(any(Team.class));
    }

    // -------------------------------------------------------------------------------------
    // Tests for getTeamById()
    // -------------------------------------------------------------------------------------

    /**
     * Should throw AccessDeniedException when no user is authenticated.
     */
    @Test
    void getTeamById_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear();

        // when / then
        assertThatThrownBy(() -> teamService.getTeamById(10))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    /**
     * Should throw TeamNotFoundException when team with given ID does not exist.
     */
    @Test
    void getTeamById_ShouldThrowTeamNotFound_WhenTeamDoesNotExist() {
        // given
        when(teamRepository.findById(10)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.getTeamById(10))
                .isInstanceOf(com.pokedexsocial.backend.exception.TeamNotFoundException.class)
                .hasMessageContaining("Team with id 10 not found");
    }

    /**
     * Should throw AccessDeniedException when team is PRIVATE and user is not owner or admin.
     */
    @Test
    void getTeamById_ShouldThrowAccessDenied_WhenPrivateAndNotOwnerOrAdmin() {
        // given
        User owner = new User();
        owner.setId(99);

        Team privateTeam = new Team();
        privateTeam.setId(10);
        privateTeam.setVisibility("PRIVATE");
        privateTeam.setUser(owner);

        when(teamRepository.findById(10)).thenReturn(Optional.of(privateTeam));

        // when / then
        assertThatThrownBy(() -> teamService.getTeamById(10))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not allowed to access this team");
    }

    /**
     * Should return team when it is PUBLIC (any user can access).
     */
    @Test
    void getTeamById_ShouldReturnTeam_WhenVisibilityIsPublic() throws Exception {
        // given
        Team publicTeam = new Team();
        publicTeam.setId(10);
        publicTeam.setVisibility("PUBLIC");
        publicTeam.setUser(dbUser);
        publicTeam.getPokemons().add(new TeamPokemon(publicTeam, bulbasaur, 1));
        publicTeam.setDescription("Public description");
        publicTeam.setCreatedAt(java.time.Instant.now());
        publicTeam.setUpdatedAt(java.time.Instant.now());

        when(teamRepository.findById(10)).thenReturn(Optional.of(publicTeam));

        // when
        var result = teamService.getTeamById(10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getVisibility()).isEqualTo("PUBLIC");
        assertThat(result.getDescription()).isEqualTo("Public description");
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    /**
     * Should return team when it is PRIVATE but owned by current user.
     */
    @Test
    void getTeamById_ShouldReturnTeam_WhenPrivateAndOwnedByCurrentUser() throws Exception {
        // given
        Team privateTeam = new Team();
        privateTeam.setId(11);
        privateTeam.setVisibility("PRIVATE");
        privateTeam.setUser(dbUser);
        privateTeam.getPokemons().add(new TeamPokemon(privateTeam, bulbasaur, 1));

        when(teamRepository.findById(11)).thenReturn(Optional.of(privateTeam));

        // when
        var result = teamService.getTeamById(11);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(11);
        assertThat(result.getVisibility()).isEqualTo("PRIVATE");
        assertThat(result.getUser().getId()).isEqualTo(dbUser.getId());
    }

    /**
     * Should return team when it is PRIVATE and user is ADMIN.
     */
    @Test
    void getTeamById_ShouldReturnTeam_WhenPrivateAndUserIsAdmin() throws Exception {
        // given
        AuthContext.setCurrentUser(new AuthenticatedUser(2, "brock", "ADMIN"));

        User owner = new User();
        owner.setId(1);

        Team privateTeam = new Team();
        privateTeam.setId(12);
        privateTeam.setVisibility("PRIVATE");
        privateTeam.setUser(owner);
        privateTeam.getPokemons().add(new TeamPokemon(privateTeam, bulbasaur, 1));

        when(teamRepository.findById(12)).thenReturn(Optional.of(privateTeam));

        // when
        var result = teamService.getTeamById(12);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVisibility()).isEqualTo("PRIVATE");
        assertThat(result.getUser().getId()).isEqualTo(1);
    }

    /**
     * Should include both types when Pokémon has type1 and type2 (indirect test of buildPokemonListDto)
     */
    @Test
    void getTeamById_ShouldIncludeBothTypes_WhenPokemonHasTwoTypes() throws Exception {
        // given
        var typeGrass = new com.pokedexsocial.backend.model.Type();
        typeGrass.setId(12);
        typeGrass.setName("Grass");

        var typePoison = new com.pokedexsocial.backend.model.Type();
        typePoison.setId(4);
        typePoison.setName("Poison");

        Pokemon bulbasaur = new Pokemon();
        bulbasaur.setId(1);
        bulbasaur.setSpecies("Bulbasaur");
        bulbasaur.setType1(typeGrass);
        bulbasaur.setType2(typePoison);

        Team team = new Team();
        team.setId(1);
        team.setVisibility("PUBLIC");
        team.setUser(dbUser);
        team.getPokemons().add(new TeamPokemon(team, bulbasaur, 1));

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // when
        TeamDto result = teamService.getTeamById(1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(1);

        var types = result.getMembers().get(0).getPokemon().types();
        assertThat(types).hasSize(2);
        assertThat(types).extracting("name").containsExactlyInAnyOrder("Grass", "Poison");
    }

    // -------------------------------------------------------------------------------------
    // Tests for deleteTeam()
    // -------------------------------------------------------------------------------------

    /**
     * Should throw AccessDeniedException when no user is authenticated.
     */
    @Test
    void deleteTeam_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear();

        // when / then
        assertThatThrownBy(() -> teamService.deleteTeam(10))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    /**
     * Should throw TeamNotFoundException when team does not exist.
     */
    @Test
    void deleteTeam_ShouldThrowTeamNotFound_WhenTeamDoesNotExist() {
        // given
        when(teamRepository.findById(10)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.deleteTeam(10))
                .isInstanceOf(com.pokedexsocial.backend.exception.TeamNotFoundException.class)
                .hasMessageContaining("Team with id 10 not found");
    }

    /**
     * Should throw AccessDeniedException when current user is neither owner nor admin.
     */
    @Test
    void deleteTeam_ShouldThrowAccessDenied_WhenNotOwnerOrAdmin() {
        // given
        User owner = new User();
        owner.setId(99);

        Team team = new Team();
        team.setId(10);
        team.setUser(owner);
        team.setVisibility("PRIVATE");

        when(teamRepository.findById(10)).thenReturn(Optional.of(team));

        // when / then
        assertThatThrownBy(() -> teamService.deleteTeam(10))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not allowed to delete this team");

        verify(teamRepository, never()).delete(any());
    }

    /**
     * Should delete successfully when current user is the team owner.
     */
    @Test
    void deleteTeam_ShouldDeleteSuccessfully_WhenUserIsOwner() throws Exception {
        // given
        Team team = new Team();
        team.setId(10);
        team.setUser(dbUser);
        team.setVisibility("PRIVATE");

        when(teamRepository.findById(10)).thenReturn(Optional.of(team));

        // when
        teamService.deleteTeam(10);

        // then
        verify(teamRepository).delete(team);
    }

    /**
     * Should delete successfully when current user is ADMIN.
     */
    @Test
    void deleteTeam_ShouldDeleteSuccessfully_WhenUserIsAdmin() throws Exception {
        // given
        AuthenticatedUser admin = new AuthenticatedUser(2, "brock", "ADMIN");
        AuthContext.setCurrentUser(admin);

        User owner = new User();
        owner.setId(1);

        Team team = new Team();
        team.setId(20);
        team.setUser(owner);

        when(teamRepository.findById(20)).thenReturn(Optional.of(team));

        // when
        teamService.deleteTeam(20);

        // then
        verify(teamRepository).delete(team);
    }


    // -------------------------------------------------------------------------------------
    // Tests for updateTeam()
    // -------------------------------------------------------------------------------------

    /**
     * Should throw AccessDeniedException when no user is authenticated.
     */
    @Test
    void updateTeam_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear();

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    /**
     * Should throw TeamNotFoundException when team does not exist.
     */
    @Test
    void updateTeam_ShouldThrowTeamNotFound_WhenTeamDoesNotExist() {
        // given
        when(teamRepository.findById(1)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(com.pokedexsocial.backend.exception.TeamNotFoundException.class)
                .hasMessageContaining("Team with id 1 not found");
    }

    /**
     * Should throw AccessDeniedException when user is neither owner nor admin.
     */
    @Test
    void updateTeam_ShouldThrowAccessDenied_WhenNotOwnerOrAdmin() {
        // given
        User owner = new User();
        owner.setId(99);

        Team team = new Team();
        team.setId(1);
        team.setUser(owner);
        team.setVisibility("PRIVATE");

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("not allowed to update this team");
    }

    /**
     * Should allow update when user is ADMIN (branch !isOwner && !isAdmin == false)
     */
    @Test
    void updateTeam_ShouldAllowUpdate_WhenUserIsAdmin() throws Exception {
        // given
        AuthenticatedUser admin = new AuthenticatedUser(99, "adminUser", "ADMIN");
        AuthContext.setCurrentUser(admin);

        // team owned by someone else
        User owner = new User();
        owner.setId(1);

        Team team = new Team();
        team.setId(10);
        team.setUser(owner);
        team.setVisibility("PRIVATE");

        // setup request
        CreateTeamRequest.MemberDto m1 = new CreateTeamRequest.MemberDto();
        m1.setPokemonId(1);
        m1.setSlot(1);

        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Updated by Admin");
        request.setDescription("Updated team");
        request.setVisibility("PUBLIC");
        request.setMembers(List.of(m1));

        Pokemon poke = new Pokemon();
        poke.setId(1);
        poke.setSpecies("Bulbasaur");

        when(teamRepository.findById(10)).thenReturn(Optional.of(team));
        when(pokemonRepository.findById(1)).thenReturn(Optional.of(poke));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        TeamDto result = teamService.updateTeam(10, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Updated by Admin");
        assertThat(result.getVisibility()).isEqualTo("PUBLIC");
        assertThat(result.getMembers()).hasSize(1);
        verify(teamRepository).save(any(Team.class));
    }

    /**
     * Should throw InvalidTeamOperationException when visibility is invalid.
     */
    @Test
    void updateTeam_ShouldThrowInvalidOperation_WhenVisibilityInvalid() {
        // given
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);

        request.setVisibility("SECRET");

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Visibility must be PUBLIC or PRIVATE");
    }

    /**
     * Should throw InvalidTeamOperationException when duplicate slots exist.
     */
    @Test
    void updateTeam_ShouldThrowInvalidOperation_WhenDuplicateSlotsExist() {
        // given
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);

        CreateTeamRequest.MemberDto m1 = new CreateTeamRequest.MemberDto();
        m1.setPokemonId(1);
        m1.setSlot(1);

        CreateTeamRequest.MemberDto m2 = new CreateTeamRequest.MemberDto();
        m2.setPokemonId(2);
        m2.setSlot(1);

        request.setMembers(List.of(m1, m2));
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Duplicate slot 1 in team");
    }

    /**
     * Should not update existing Pokémon when the same Pokémon ID is provided (branch false)
     */
    @Test
    void updateTeam_ShouldNotChangePokemon_WhenSamePokemonId() throws Exception {
        // given
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);
        team.setVisibility("PRIVATE");

        // Pokémon esistente
        Pokemon bulbasaur = new Pokemon();
        bulbasaur.setId(1);
        bulbasaur.setSpecies("Bulbasaur");

        // Team con un membro esistente (slot 1)
        TeamPokemon teamPokemon = spy(new TeamPokemon(team, bulbasaur, 1));
        team.getPokemons().add(teamPokemon);

        // Request con stesso Pokémon nello stesso slot
        CreateTeamRequest.MemberDto member = new CreateTeamRequest.MemberDto();
        member.setPokemonId(1); // stesso ID
        member.setSlot(1);

        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("No Change Team");
        request.setDescription("Should not change Pokémon");
        request.setVisibility("PUBLIC");
        request.setMembers(List.of(member));

        // Mock repository
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(pokemonRepository.findById(1)).thenReturn(Optional.of(bulbasaur));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = teamService.updateTeam(1, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getPokemon().species()).isEqualTo("Bulbasaur");

        // Verifica che non sia stato chiamato setPokemon (ramo false)
        verify(teamPokemon, never()).setPokemon(any());
    }


    /**
     * Should remove team members whose slot is no longer present in the update request
     */
    @Test
    void updateTeam_ShouldRemoveMissingSlots_WhenNotInRequest() throws Exception {
        // given
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);
        team.setVisibility("PRIVATE");

        // Pokémon esistenti
        Pokemon p1 = new Pokemon();
        p1.setId(1);
        p1.setSpecies("Bulbasaur");

        Pokemon p2 = new Pokemon();
        p2.setId(2);
        p2.setSpecies("Ivysaur");

        // Team con due membri (slot 1 e 2)
        team.getPokemons().add(new TeamPokemon(team, p1, 1));
        team.getPokemons().add(new TeamPokemon(team, p2, 2));

        // Request con solo lo slot 1 (slot 2 verrà rimosso)
        CreateTeamRequest.MemberDto m1 = new CreateTeamRequest.MemberDto();
        m1.setPokemonId(1);
        m1.setSlot(1);

        CreateTeamRequest request = new CreateTeamRequest();
        request.setName("Trimmed Team");
        request.setDescription("Should remove slot 2");
        request.setVisibility("PUBLIC");
        request.setMembers(List.of(m1));

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(pokemonRepository.findById(1)).thenReturn(Optional.of(p1));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        TeamDto result = teamService.updateTeam(1, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getMembers()).hasSize(1);
        assertThat(result.getMembers().get(0).getSlot()).isEqualTo(1);
        assertThat(result.getMembers().get(0).getPokemon().species()).isEqualTo("Bulbasaur");

        // verifica che il membro dello slot 2 sia stato effettivamente rimosso
        assertThat(team.getPokemons())
                .extracting(TeamPokemon::getSlot)
                .containsExactly(1);

        verify(teamRepository).save(any(Team.class));
    }

    /**
     * Should throw InvalidTeamOperationException when Pokémon in request not found.
     */
    @Test
    void updateTeam_ShouldThrowInvalidOperation_WhenPokemonNotFound() {
        // given
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);

        when(teamRepository.findById(1)).thenReturn(Optional.of(team));
        when(pokemonRepository.findById(anyInt())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.updateTeam(1, request))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Pokemon not found");
    }

    /**
     * Should update team successfully: replace, add, and remove members.
     */
    @Test
    void updateTeam_ShouldUpdateTeamSuccessfully_WhenRequestValid() throws Exception {
        // given
        // existing team setup
        Team team = new Team();
        team.setId(1);
        team.setUser(dbUser);
        team.setVisibility("PRIVATE");

        Pokemon oldPokemon = new Pokemon();
        oldPokemon.setId(100);
        oldPokemon.setSpecies("OldMon");
        team.getPokemons().add(new TeamPokemon(team, oldPokemon, 1)); // slot 1 existing

        // new members
        CreateTeamRequest.MemberDto m1 = new CreateTeamRequest.MemberDto();
        m1.setPokemonId(1);
        m1.setSlot(1); // replaces old

        CreateTeamRequest.MemberDto m2 = new CreateTeamRequest.MemberDto();
        m2.setPokemonId(2);
        m2.setSlot(2); // new addition

        request.setMembers(List.of(m1, m2));
        request.setVisibility("PUBLIC");
        request.setDescription("Updated team");

        // mock repositories
        when(teamRepository.findById(1)).thenReturn(Optional.of(team));

        Pokemon newPoke1 = new Pokemon();
        newPoke1.setId(1);
        newPoke1.setSpecies("Bulbasaur");

        Pokemon newPoke2 = new Pokemon();
        newPoke2.setId(2);
        newPoke2.setSpecies("Ivysaur");


        when(pokemonRepository.findById(1)).thenReturn(Optional.of(newPoke1));
        when(pokemonRepository.findById(2)).thenReturn(Optional.of(newPoke2));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        var result = teamService.updateTeam(1, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getVisibility()).isEqualTo("PUBLIC");
        assertThat(result.getMembers()).hasSize(2);
        assertThat(result.getMembers().get(0).getPokemon().species()).isIn("Bulbasaur", "Ivysaur");

        ArgumentCaptor<Team> captor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(captor.capture());
        Team saved = captor.getValue();

        assertThat(saved.getDescription()).isEqualTo("Updated team");
    }

    // -------------------------------------------------------------------------------------
    // Tests for getUserTeams()
    // -------------------------------------------------------------------------------------

    /**
     * Should throw AccessDeniedException when no user is authenticated.
     */
    @Test
    void getUserTeams_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear();

        // when / then
        assertThatThrownBy(() -> teamService.getUserTeams(mock(org.springframework.data.domain.Pageable.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    /**
     * Should throw InvalidTeamOperationException when authenticated user not found in DB.
     */
    @Test
    void getUserTeams_ShouldThrowInvalidOperation_WhenAuthenticatedUserNotFoundInDb() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.getUserTeams(mock(org.springframework.data.domain.Pageable.class)))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Authenticated user not found");
    }

    /**
     * Should return paged response of user's teams when authenticated user exists.
     */
    @Test
    void getUserTeams_ShouldReturnPagedResponse_WhenUserExists() throws Exception {
        // given
        var pageable = mock(org.springframework.data.domain.Pageable.class);
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));

        Team team1 = new Team();
        team1.setId(1);
        team1.setName("Team A");
        team1.setUser(dbUser);
        team1.getPokemons().add(new TeamPokemon(team1, bulbasaur, 1));

        Team team2 = new Team();
        team2.setId(2);
        team2.setName("Team B");
        team2.setUser(dbUser);
        team2.getPokemons().add(new TeamPokemon(team2, bulbasaur, 1));

        org.springframework.data.domain.Page<Team> teamPage =
                new org.springframework.data.domain.PageImpl<>(List.of(team1, team2));

        when(teamRepository.findByUser(dbUser, pageable)).thenReturn(teamPage);

        // when
        var result = teamService.getUserTeams(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Team A");
        assertThat(result.getPage()).isEqualTo(0); // PageImpl default number = 0

        verify(teamRepository).findByUser(dbUser, pageable);
    }

    // -------------------------------------------------------------------------------------
    // Tests for getAllTeams()
    // -------------------------------------------------------------------------------------

    /**
     * Should throw AccessDeniedException when no user is authenticated.
     */
    @Test
    void getAllTeams_ShouldThrowAccessDenied_WhenUserNotAuthenticated() {
        // given
        AuthContext.clear();

        // when / then
        assertThatThrownBy(() -> teamService.getAllTeams(mock(org.springframework.data.domain.Pageable.class)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You must be logged in");
    }

    /**
     * Should throw InvalidTeamOperationException when authenticated user not found in DB (non-admin).
     */
    @Test
    void getAllTeams_ShouldThrowInvalidOperation_WhenAuthenticatedUserNotFoundInDb() {
        // given
        when(userRepository.findById(authUser.id())).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> teamService.getAllTeams(mock(org.springframework.data.domain.Pageable.class)))
                .isInstanceOf(InvalidTeamOperationException.class)
                .hasMessageContaining("Authenticated user not found");
    }

    /**
     * Should return all teams when user is ADMIN.
     */
    @Test
    void getAllTeams_ShouldReturnAllTeams_WhenUserIsAdmin() throws Exception {
        // given
        AuthenticatedUser admin = new AuthenticatedUser(2, "brock", "ADMIN");
        AuthContext.setCurrentUser(admin);

        var pageable = mock(org.springframework.data.domain.Pageable.class);

        Team team1 = new Team();
        team1.setId(1);
        team1.setUser(dbUser);
        team1.getPokemons().add(new TeamPokemon(team1, bulbasaur, 1));

        Team team2 = new Team();
        team2.setId(2);
        team2.setUser(dbUser);
        team2.getPokemons().add(new TeamPokemon(team2, bulbasaur, 1));

        org.springframework.data.domain.Page<Team> page =
                new org.springframework.data.domain.PageImpl<>(List.of(team1, team2));

        when(teamRepository.findAll(pageable)).thenReturn(page);

        // when
        var result = teamService.getAllTeams(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        verify(teamRepository).findAll(pageable);
        verify(teamRepository, never()).findAllVisibleForUser(anyInt(), any());
    }

    /**
     * Should return visible and own teams when user is regular USER.
     */
    @Test
    void getAllTeams_ShouldReturnVisibleTeams_WhenUserIsRegular() throws Exception {
        // given
        var pageable = mock(org.springframework.data.domain.Pageable.class);
        when(userRepository.findById(authUser.id())).thenReturn(Optional.of(dbUser));

        Team t1 = new Team();
        t1.setId(1);
        t1.setUser(dbUser);
        t1.setVisibility("PUBLIC");
        t1.getPokemons().add(new TeamPokemon(t1, bulbasaur, 1));

        Team t2 = new Team();
        t2.setId(2);
        t2.setUser(dbUser);
        t2.setVisibility("PRIVATE");
        t2.getPokemons().add(new TeamPokemon(t2, bulbasaur, 1));

        org.springframework.data.domain.Page<Team> page =
                new org.springframework.data.domain.PageImpl<>(List.of(t1, t2));

        when(teamRepository.findAllVisibleForUser(dbUser.getId(), pageable)).thenReturn(page);

        // when
        var result = teamService.getAllTeams(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo(1);

        verify(teamRepository).findAllVisibleForUser(dbUser.getId(), pageable);
        verify(teamRepository, never()).findAll(pageable);
    }
}
