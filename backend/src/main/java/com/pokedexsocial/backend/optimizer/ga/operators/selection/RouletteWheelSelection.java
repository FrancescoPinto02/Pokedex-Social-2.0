package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component("RouletteWheel")
public class RouletteWheelSelection<T extends Individual> extends SelectionOperator<T> {

    private static class WheelElement<T extends Individual> {
        final T individual;
        final double endPosition;

        WheelElement(T individual, double endPosition) {
            this.individual = individual;
            this.endPosition = endPosition;
        }
    }

    @Override
    public Population<T> apply(Population<T> population, Random rand) throws CloneNotSupportedException {

        int N = population.size();
        List<T> individuals = new ArrayList<>(population);

        // Calcolo fitness totale
        double totalFitness = individuals.stream()
                .mapToDouble(Individual::getFitness)
                .sum();

        // Caso fitness nulla → selezione casuale
        if (totalFitness <= 0) {
            Population<T> randomPop = population.clone();
            randomPop.clear();
            for (int i = 0; i < N; i++) {
                T chosen = individuals.get(rand.nextInt(N));
                randomPop.add((T) chosen.clone());
            }
            return randomPop;
        }

        // =========================================
        // Costruzione ruota cumulativa (0 → 1)
        // =========================================
        List<WheelElement<T>> wheel = new ArrayList<>(N);
        double cumulative = 0;

        for (T ind : individuals) {
            cumulative += ind.getFitness() / totalFitness;
            wheel.add(new WheelElement<>(ind, cumulative));
        }

        // =========================================
        // Costruzione nuova popolazione tramite binary search
        // =========================================
        Population<T> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        for (int i = 0; i < N; i++) {
            double r = rand.nextDouble();
            int index = binarySearchWheel(wheel, r);
            newPopulation.add((T) wheel.get(index).individual.clone());
        }

        return newPopulation;
    }

    // Ricerca binaria sulla ruota
    private int binarySearchWheel(List<WheelElement<T>> wheel, double value) {
        int low = 0;
        int high = wheel.size() - 1;

        while (low < high) {
            int mid = (low + high) / 2;
            if (wheel.get(mid).endPosition >= value) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }
}
