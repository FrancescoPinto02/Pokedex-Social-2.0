package com.pokedexsocial.backend.dto;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;

import java.util.List;

public class OptimizationResultDTO {
    private PokemonTeamGA bestTeam;
    private double bestFitness;
    private int iterations;
    private List<String> log;

    public OptimizationResultDTO(PokemonTeamGA bestTeam, double bestFitness, int iterations, List<String> log) {
        this.bestTeam = bestTeam;
        this.bestFitness = bestFitness;
        this.iterations = iterations;
        this.log = log;
    }

    public PokemonTeamGA getBestTeam() {
        return bestTeam;
    }

    public void setBestTeam(PokemonTeamGA bestTeam) {
        this.bestTeam = bestTeam;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public void setBestFitness(double bestFitness) {
        this.bestFitness = bestFitness;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }
}
