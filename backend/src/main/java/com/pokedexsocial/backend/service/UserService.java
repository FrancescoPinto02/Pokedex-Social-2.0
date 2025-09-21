package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.exception.UserNotFoundException;
import com.pokedexsocial.backend.model.User;
import com.pokedexsocial.backend.repository.UserRepository;
import com.pokedexsocial.backend.security.AuthContext;
import com.pokedexsocial.backend.security.AuthenticatedUser;
import org.springframework.stereotype.Service;

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

    /**
     * Constructs a new UserService with the given repository.
     *
     * @param userRepository the repository for accessing user data
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}