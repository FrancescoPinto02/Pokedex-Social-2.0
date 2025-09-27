package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.CreateTeamRequest;
import com.pokedexsocial.backend.dto.PagedResponse;
import com.pokedexsocial.backend.dto.TeamDto;
import com.pokedexsocial.backend.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

/**
 * REST controller responsible for managing Pokémon teams.
 *
 * <p>Endpoints:</p>
 * <ul>
 *     <li>POST /teams - create a new team</li>
 *     <li>GET /teams/{id} - retrieve a team by ID</li>
 *     <li>DELETE /teams/{id} - delete a team by ID</li>
 *     <li>PUT /teams/{id} - update a team by ID</li>
 *     <li>GET /teams/my - retrieve teams owned by the authenticated user (with pagination)</li>
 *     <li>GET /teams - retrieve all teams (with pagination, visibility depends on role)</li>
 * </ul>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *     <li>Users can create new teams.</li>
 *     <li>Users can view public teams and their own teams.</li>
 *     <li>Users can update or delete only their own teams.</li>
 *     <li>Admins can view, update, and delete all teams regardless of ownership or visibility.</li>
 * </ul>
 */
@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;

    /**
     * Constructs a new {@code TeamController} with the given service.
     *
     * @param teamService the service responsible for team-related operations
     */
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Creates a new team for the authenticated user.
     *
     * @param request the {@link CreateTeamRequest} containing team details and members
     * @return a {@link ResponseEntity} containing the created {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     */
    @PostMapping
    public ResponseEntity<TeamDto> createTeam(@Valid @RequestBody CreateTeamRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(teamService.createTeam(request));
    }

    /**
     * Retrieves a team by its ID.
     *
     * <p>Public teams are always visible. Private teams can only be accessed
     * by their owner or an admin.</p>
     *
     * @param id the ID of the team to retrieve
     * @return a {@link ResponseEntity} containing the {@link TeamDto}
     * @throws AccessDeniedException if the team is private and the user is not the owner or an admin
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamDto> getTeamById(@PathVariable Integer id) throws AccessDeniedException {
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    /**
     * Deletes a team by its ID.
     *
     * <p>Only the team owner or an admin can delete a team.</p>
     *
     * @param id the ID of the team to delete
     * @return a {@link ResponseEntity} with status {@code 204 No Content} if successful
     * @throws AccessDeniedException if the user is not authorized to delete this team
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Integer id) throws AccessDeniedException {
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates a team by its ID.
     *
     * <p>Only the team owner or an admin can update a team. The request body must
     * include updated details and members. If members are provided, the roster
     * will be updated accordingly.</p>
     *
     * @param id the ID of the team to update
     * @param request the {@link CreateTeamRequest} containing updated details
     * @return a {@link ResponseEntity} containing the updated {@link TeamDto}
     * @throws AccessDeniedException if the user is not authorized to update this team
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamDto> updateTeam(@PathVariable Integer id, @Valid @RequestBody CreateTeamRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    /**
     * Retrieves teams owned by the authenticated user, with pagination support.
     *
     * @param pageable pagination information (page, size, sort)
     * @return a {@link ResponseEntity} containing a {@link PagedResponse} of {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     */
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<TeamDto>> getMyTeams(Pageable pageable) throws AccessDeniedException {
        return ResponseEntity.ok(teamService.getUserTeams(pageable));
    }

    /**
     * Retrieves all teams, with pagination support.
     *
     * <p>Regular users can view only their own teams and public teams,
     * while admins can view all teams.</p>
     *
     * @param pageable pagination information (page, size, sort)
     * @return a {@link ResponseEntity} containing a {@link PagedResponse} of {@link TeamDto}
     * @throws AccessDeniedException if the user is not authenticated
     */
    @GetMapping
    public ResponseEntity<PagedResponse<TeamDto>> getAllTeams(Pageable pageable) throws AccessDeniedException {
        return ResponseEntity.ok(teamService.getAllTeams(pageable));
    }
}