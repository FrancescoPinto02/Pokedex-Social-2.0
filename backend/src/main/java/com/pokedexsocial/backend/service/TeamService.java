package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.*;
import com.pokedexsocial.backend.exception.InvalidTeamOperationException;
import com.pokedexsocial.backend.exception.TeamNotFoundException;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Team;
import com.pokedexsocial.backend.model.TeamPokemon;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.PokemonRepository;
import com.pokedexsocial.backend.repository.TeamRepository;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for team-related business logic.
 *
 * <p>This layer handles operations such as:</p>
 * <ul>
 *   <li>Creating, updating and deleting teams.</li>
 *   <li>Fetching teams by ID with visibility and authorization checks.</li>
 *   <li>Listing teams with pagination (user-specific or global view).</li>
 * </ul>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *   <li>Regular users can manage only their own teams and view public teams.</li>
 *   <li>Admins can view and manage all teams.</li>
 * </ul>
 *
 * <p>Validation ensures constraints such as unique slots, valid Pokémon references,
 * and proper visibility values (PUBLIC/PRIVATE).</p>
 */
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final PokemonRepository pokemonRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository,
                       PokemonRepository pokemonRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.pokemonRepository = pokemonRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new team for the authenticated user.
     *
     * @param request the team creation request with details and members
     * @return the created {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     * @throws InvalidTeamOperationException if the request is invalid (e.g., duplicate slots, invalid Pokémon)
     */
    @Transactional
    public TeamDto createTeam(CreateTeamRequest request) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();
        User user = userRepository.findById(current.id())
                .orElseThrow(() -> new InvalidTeamOperationException("Authenticated user not found"));

        validateVisibility(request.getVisibility());
        validateSlots(request.getMembers());

        Team team = new Team();
        team.setUser(user);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setVisibility(request.getVisibility().toUpperCase());

        // aggiungi membri
        request.getMembers().forEach(member -> {
            Pokemon pokemon = pokemonRepository.findById(member.getPokemonId())
                    .orElseThrow(() -> new InvalidTeamOperationException("Pokemon not found with id " + member.getPokemonId()));
            team.getPokemons().add(new TeamPokemon(team, pokemon, member.getSlot()));
        });

        Team saved = teamRepository.save(team);
        return mapToDto(saved);
    }

    /**
     * Retrieves a team by ID.
     *
     * <p>Public teams are always visible. Private teams can be accessed only
     * by their owner or an ADMIN.</p>
     *
     * @param teamId the ID of the team
     * @return the {@link TeamDto}
     * @throws AccessDeniedException if the user is not authorized
     * @throws TeamNotFoundException if no team with the given ID exists
     */
    @Transactional(readOnly = true)
    public TeamDto getTeamById(Integer teamId) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team with id " + teamId + " not found"));

        // Se privato → accesso solo all'owner o admin
        if ("PRIVATE".equalsIgnoreCase(team.getVisibility())
                && !team.getUser().getId().equals(current.id())
                && !"ADMIN".equalsIgnoreCase(current.role())) {
            throw new AccessDeniedException("You are not allowed to access this team");
        }

        return mapToDto(team);
    }

    /**
     * Deletes a team by ID.
     *
     * <p>Allowed only for the owner or an ADMIN.</p>
     *
     * @param teamId the ID of the team to delete
     * @throws AccessDeniedException if the user is not authorized
     * @throws TeamNotFoundException if no team with the given ID exists
     */
    @Transactional
    public void deleteTeam(Integer teamId) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team with id " + teamId + " not found"));

        // Solo owner o admin può cancellare
        boolean isOwner = team.getUser().getId().equals(current.id());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(current.role());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to delete this team");
        }

        teamRepository.delete(team);
    }

    /**
     * Updates an existing team.
     *
     * <p>Allowed only for the owner or an ADMIN. Updates team info and members.</p>
     *
     * @param teamId the ID of the team to update
     * @param request the update request with new details and members
     * @return the updated {@link TeamDto}
     * @throws AccessDeniedException if the user is not authorized
     * @throws TeamNotFoundException if no team with the given ID exists
     */
    @Transactional
    public TeamDto updateTeam(Integer teamId, CreateTeamRequest request) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException("Team with id " + teamId + " not found"));

        boolean isOwner = team.getUser().getId().equals(current.id());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(current.role());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You are not allowed to update this team");
        }

        validateVisibility(request.getVisibility());
        validateSlots(request.getMembers());

        // Aggiorna dati base
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setVisibility(request.getVisibility().toUpperCase());

        // Mappa attuale slot -> TeamPokemon
        Map<Integer, TeamPokemon> currentMembers = team.getPokemons().stream()
                .collect(Collectors.toMap(TeamPokemon::getSlot, tp -> tp));

        // Slot richiesti
        Set<Integer> requestedSlots = request.getMembers().stream()
                .map(CreateTeamRequest.MemberDto::getSlot)
                .collect(Collectors.toSet());

        // Aggiorna o aggiungi membri
        for (CreateTeamRequest.MemberDto member : request.getMembers()) {
            TeamPokemon existing = currentMembers.get(member.getSlot());
            Pokemon pokemon = pokemonRepository.findById(member.getPokemonId())
                    .orElseThrow(() -> new InvalidTeamOperationException("Pokemon not found with id " + member.getPokemonId()));

            if (existing != null) {
                // aggiorna solo se il Pokémon è cambiato
                if (!existing.getPokemon().getId().equals(pokemon.getId())) {
                    existing.setPokemon(pokemon);
                }
            } else {
                // nuovo membro
                team.getPokemons().add(new TeamPokemon(team, pokemon, member.getSlot()));
            }
        }

        // Rimuovi gli slot che non sono più presenti
        team.getPokemons().removeIf(tp -> !requestedSlots.contains(tp.getSlot()));

        Team saved = teamRepository.save(team);
        return mapToDto(saved);
    }

    /**
     * Retrieves all teams owned by the authenticated user with pagination.
     *
     * @param pageable pagination info (page, size, sort)
     * @return a {@link PagedResponse} of {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     */
    @Transactional(readOnly = true)
    public PagedResponse<TeamDto> getUserTeams(Pageable pageable) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();

        User user = userRepository.findById(current.id())
                .orElseThrow(() -> new InvalidTeamOperationException("Authenticated user not found"));

        Page<Team> teams = teamRepository.findByUser(user, pageable);

        Page<TeamDto> dtoPage = teams.map(this::mapToDto);

        return PagedResponse.from(dtoPage);
    }

    /**
     * Retrieves all teams with pagination.
     *
     * <p>Users see their own and public teams, ADMINs see all teams.</p>
     *
     * @param pageable pagination info (page, size, sort)
     * @return a {@link PagedResponse} of {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     */
    @Transactional(readOnly = true)
    public PagedResponse<TeamDto> getAllTeams(Pageable pageable) throws AccessDeniedException {
        AuthenticatedUser current = getAuthenticatedUser();

        Page<Team> teams;
        if ("ADMIN".equalsIgnoreCase(current.role())) {
            // Admin: tutti i team
            teams = teamRepository.findAll(pageable);
        } else {
            // User: team pubblici + propri
            User user = userRepository.findById(current.id())
                    .orElseThrow(() -> new InvalidTeamOperationException("Authenticated user not found"));
            teams = teamRepository.findAllVisibleForUser(user.getId(), pageable);
        }

        Page<TeamDto> dtoPage = teams.map(this::mapToDto);
        return PagedResponse.from(dtoPage);
    }


    private AuthenticatedUser getAuthenticatedUser() throws AccessDeniedException {
        AuthenticatedUser current = AuthContext.getCurrentUser();
        if (current == null) {
            throw new AccessDeniedException("You must be logged in");
        }
        return current;
    }

    private void validateVisibility(String visibility) {
        if (visibility == null) {
            throw new InvalidTeamOperationException("Visibility is required");
        }
        String upper = visibility.toUpperCase();
        if (!upper.equals("PUBLIC") && !upper.equals("PRIVATE")) {
            throw new InvalidTeamOperationException("Visibility must be PUBLIC or PRIVATE");
        }
    }

    private void validateSlots(List<CreateTeamRequest.MemberDto> members) {
        Set<Integer> usedSlots = new HashSet<>();
        for (CreateTeamRequest.MemberDto member : members) {
            if (!usedSlots.add(member.getSlot())) {
                throw new InvalidTeamOperationException("Duplicate slot " + member.getSlot() + " in team");
            }
        }
    }

    private TeamDto mapToDto(Team team) {
        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setDescription(team.getDescription());
        dto.setVisibility(team.getVisibility());
        dto.setCreatedAt(team.getCreatedAt());
        dto.setUpdatedAt(team.getUpdatedAt());

        dto.setUser(new TeamDto.UserSummaryDto(
                team.getUser().getId(),
                team.getUser().getUsername()
        ));

        dto.setMembers(team.getPokemons().stream()
                .map(tp -> new TeamDto.MemberDto(
                        tp.getSlot(),
                        buildPokemonListDto(tp.getPokemon())
                ))
                .collect(Collectors.toList())
        );

        return dto;
    }

    private PokemonListDto buildPokemonListDto(Pokemon p) {
        List<TypeDto> types = new ArrayList<>();
        if (p.getType1() != null) {
            types.add(new TypeDto(p.getType1().getId(), p.getType1().getName()));
        }
        if (p.getType2() != null) {
            types.add(new TypeDto(p.getType2().getId(), p.getType2().getName()));
        }

        return new PokemonListDto(
                p.getId(),
                p.getNdex(),
                p.getSpecies(),
                p.getForme(),
                p.getPokemonClass(),
                types,
                p.getImageUrl()
        );
    }
}
