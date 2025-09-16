package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.UserInfoDTO;
import com.pokedexsocial.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserInfoDTO> getUserById(@PathVariable Integer id) throws AccessDeniedException {
        return ResponseEntity.ok(userService.getUserInfo(id));
    }
}
