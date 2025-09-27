package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.UpdateUserRequest;
import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.exception.InvalidUserOperationException;
import com.pokedexsocial.backend.exception.UserNotFoundException;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

/**
 * Service responsible for user-related business logic.
 *
 * <p>This service layer handles:
 * <ul>
 *   <li>Fetching user information.</li>
 *   <li>Performing authorization checks based on the authenticated user's role and ID.</li>
 *   <li>Throwing domain-specific exceptions when access is denied or user not found.</li>
 * </ul>
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructs a new UserService with the given repository.
     *
     * @param userRepository the repository for accessing user data
     */
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves user information by ID while enforcing authorization rules.
     *
     * <p>If the authenticated user is not the owner of the requested ID and does not have
     * the ADMIN role, an {@link AccessDeniedException} is thrown.</p>
     *
     * @param id the ID of the user to retrieve
     * @return a {@link UserInfoDTO} representing the user's information
     * @throws UserNotFoundException if no user with the given ID exists
     * @throws AccessDeniedException if the current user is not authorized to access this resource
     */
    public UserInfoDTO getUserInfo(Integer id) throws AccessDeniedException {
        AuthenticatedUser current = AuthContext.getCurrentUser();

        if (!id.equals(current.id()) && !"ADMIN".equals(current.role())) {
            throw new AccessDeniedException("You cannot access another user's data");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return UserInfoDTO.fromEntity(user);
    }

    /**
     * Deletes a user by ID.
     *
     * <p>Allowed only for the account owner or an ADMIN.</p>
     *
     * @param id the ID of the user to delete
     * @throws AccessDeniedException if the authenticated user is not authorized
     * @throws UserNotFoundException if no user with the given ID exists
     */
    @Transactional
    public void deleteUser(Integer id) throws AccessDeniedException {
        AuthenticatedUser current = AuthContext.getCurrentUser();

        if (!id.equals(current.id()) && !"ADMIN".equals(current.role())) {
            throw new AccessDeniedException("You cannot delete another user's profile");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        userRepository.delete(user);
    }

    /**
     * Updates user account information.
     *
     * <p>Allows updating profile fields and optionally changing the password,
     * which requires both old and new passwords.</p>
     *
     * @param id the ID of the user to update
     * @param request the update request with new values
     * @return updated {@link UserInfoDTO}
     * @throws AccessDeniedException if the authenticated user is not authorized
     * @throws UserNotFoundException if no user with the given ID exists
     * @throws InvalidUserOperationException if the old password is incorrect
     */
    @Transactional
    public UserInfoDTO updateUser(Integer id, UpdateUserRequest request) throws AccessDeniedException {
        AuthenticatedUser current = AuthContext.getCurrentUser();

        if (!id.equals(current.id()) && !"ADMIN".equals(current.role())) {
            throw new AccessDeniedException("You cannot update another user's account");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Aggiorna campi base se forniti
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());

        // Cambio password se richiesto
        if (request.getOldPassword() != null && request.getNewPassword() != null) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new InvalidUserOperationException("Old password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        User saved = userRepository.save(user);
        return UserInfoDTO.fromEntity(saved);
    }
}