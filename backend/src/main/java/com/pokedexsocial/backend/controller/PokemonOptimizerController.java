package com.pokedexsocial.backend.controller;

import com.pokedexsocial.backend.dto.OptimizationResultDTO;
import com.pokedexsocial.backend.service.TeamOptimizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/optimizer")
public class PokemonOptimizerController {

    private final TeamOptimizationService optimizationService;

    public PokemonOptimizerController(TeamOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }


    @GetMapping("/run")
    public OptimizationResultDTO runOptimization() throws CloneNotSupportedException {
        return optimizationService.optimize();
    }
}
