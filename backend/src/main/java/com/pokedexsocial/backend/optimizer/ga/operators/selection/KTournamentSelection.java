package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component("KTournament")
public class KTournamentSelection <T extends Individual> extends SelectionOperator<T>{

    private final int tournamentSize = 5;
    private final int selectionSize = 100;
    private final boolean proportional = true;

    @Override
    public Population<T> apply(Population<T> population, Random rand) throws CloneNotSupportedException {
        Population<T> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        List<T> populationList = new ArrayList<>();
        populationList.addAll(population);

        while(newPopulation.size() < selectionSize){
            List<T> tournament = new ArrayList<>();
            for(int i = 0; i< tournamentSize; i++){
                int randomIndex = rand.nextInt(populationList.size());
                tournament.add(populationList.get(randomIndex));
            }

            T best = null;
            if(!proportional){
                best = Collections.max(tournament);
            }
            else{
                best = getProportionalWinner(tournament, rand);
            }

            newPopulation.add((T) best.clone());
        }

        return newPopulation;
    }


    private T getProportionalWinner(List<T> tournament, Random rand) {
        double totalFitness = tournament.stream()
                .mapToDouble(T::getFitness)
                .sum();

        // fallback per evitare divisione per 0
        if (totalFitness <= 0) {
            return Collections.max(tournament);
        }

        double currentPosition = 0.0;
        double[] startPositions = new double[tournamentSize];
        double[] sizes = new double[tournamentSize];

        for (int i = 0; i < tournamentSize; i++) {
            T participant = tournament.get(i);
            double relativeFitness = participant.getFitness() / totalFitness;
            startPositions[i] = currentPosition;
            sizes[i] = relativeFitness;
            currentPosition += relativeFitness;
        }

        double pointer = rand.nextDouble();
        for (int i = 0; i < tournament.size(); i++) {
            if (startPositions[i] <= pointer && pointer < startPositions[i] + sizes[i]) {
                return tournament.get(i);
            }
        }

        // fallback in caso di errore numerico marginale
        return tournament.get(tournamentSize - 1);
    }
}
