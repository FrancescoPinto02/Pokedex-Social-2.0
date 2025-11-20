package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component("KTournament")
public class KTournamentSelection<T extends Individual> extends SelectionOperator<T> {

    private int tournamentSize = 5;
    private boolean proportional = false;  // puoi settarlo da fuori

    @Override
    public Population<T> apply(Population<T> population, Random rand) throws CloneNotSupportedException {

        int N = population.size();
        List<T> individuals = new ArrayList<>(population);

        // Nuova popolazione
        Population<T> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        while (newPopulation.size() < N) {

            // ================================
            // Estrazione torneo SENZA rimpiazzo
            // ================================
            Collections.shuffle(individuals, rand);
            List<T> tournament = individuals.subList(0, tournamentSize);

            // ================================
            // Selezione vincitore
            // ================================
            T winner;

            if (proportional) {
                winner = proportionalWinner(tournament, rand);
            } else {
                winner = Collections.max(tournament);
            }

            newPopulation.add((T) winner.clone());
        }

        return newPopulation;
    }

    private T proportionalWinner(List<T> tournament, Random rand) {

        double totalFitness = tournament.stream()
                .mapToDouble(Individual::getFitness)
                .sum();

        if (totalFitness <= 0) {
            return Collections.max(tournament);
        }

        double pointer = rand.nextDouble();
        double cumulative = 0;

        for (T ind : tournament) {
            cumulative += ind.getFitness() / totalFitness;
            if (pointer <= cumulative) {
                return ind;
            }
        }

        return tournament.get(tournament.size() - 1);
    }
}
