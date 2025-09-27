package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.UpdateUserRequest;
import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

/**
 * REST controller responsible for user account management.
 *
 * <p>Endpoints:</p>
 * <ul>
 *     <li>GET /user/{id} - retrieve user information</li>
 *     <li>DELETE /user/{id} - delete a user account</li>
 *     <li>PUT /user/{id} - update user account details (profile info or password)</li>
 * </ul>
 *
 * <p>Authorization rules:</p>
 * <ul>
 *     <li>Users can only access, update, or delete their own account.</li>
 *     <li>Admins can access, update, or delete any account.</li>
 * </ul>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a new {@code UserController} with the given user service.
     *
     * @param userService the service responsible for user-related operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves user information by ID.
     *
     * <p>If the authenticated user is not the owner of the requested account
     * and does not have the ADMIN role, an {@link AccessDeniedException} is thrown.</p>
     *
     * @param id the ID of the user to retrieve
     * @return a {@link ResponseEntity} containing a {@link UserInfoDTO} with user details
     * @throws AccessDeniedException if the authenticated user is not authorized to access this resource
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDTO> getUserById(@PathVariable Integer id) throws AccessDeniedException {
        return ResponseEntity.ok(userService.getUserInfo(id));
    }

    /**
     * Deletes a user account by ID.
     *
     * <p>If the authenticated user is not the owner of the requested account
     * and does not have the ADMIN role, an {@link AccessDeniedException} is thrown.</p>
     *
     * @param id the ID of the user to delete
     * @return a {@link ResponseEntity} with HTTP 204 No Content if successful
     * @throws AccessDeniedException if the authenticated user is not authorized to delete this resource
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) throws AccessDeniedException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Updates user account information.
     *
     * <p>The request can include profile changes (first name, last name, email)
     * and/or a password change. To change the password, both the old and new
     * password must be provided. The old password is validated against the
     * stored hash before updating.</p>
     *
     * <p>If the authenticated user is not the owner of the requested account
     * and does not have the ADMIN role, an {@link AccessDeniedException} is thrown.</p>
     *
     * @param id      the ID of the user to update
     * @param request the {@link UpdateUserRequest} containing updated information
     * @return a {@link ResponseEntity} containing the updated {@link UserInfoDTO}
     * @throws AccessDeniedException if the authenticated user is not authorized to update this resource
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserInfoDTO> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateUserRequest request) throws AccessDeniedException {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
