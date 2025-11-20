package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Component("RankSelection")
public class RankSelection<T extends Individual> extends SelectionOperator<T> {

    @Override
    public Population<T> apply(Population<T> population, Random rand) throws CloneNotSupportedException {

        int N = population.size();
        if (N == 0) return population;

        // Ordina la popolazione in base alla fitness (crescente → decrescente)
        List<T> sorted = new ArrayList<>(population);
        sorted.sort(Comparator.comparingDouble(Individual::getFitness)); // peggiori → migliori

        // Calcolo dei rank: 1 (peggiore) → N (migliore)
        double totalRankSum = (N * (N + 1)) / 2.0;

        // Ricalcolo: ruota come la roulette wheel ma su rank virtuali
        List<RankElement<T>> rankWheel = new ArrayList<>();
        double cursor = 0.0;

        for (int i = 0; i < N; i++) {
            double rank = i + 1; // 1 → N
            double prob = rank / totalRankSum;

            rankWheel.add(new RankElement<>(sorted.get(i), cursor, prob));
            cursor += prob;
        }

        // Costruisce la nuova popolazione
        Population<T> newPopulation = population.clone();
        newPopulation.setId(population.getId() + 1);
        newPopulation.clear();

        for (int i = 0; i < N; i++) {
            double pointer = rand.nextDouble();
            for (RankElement<T> e : rankWheel) {
                if (pointer >= e.start && pointer < e.start + e.size) {
                    newPopulation.add((T) e.individual.clone());
                    break;
                }
            }
        }

        return newPopulation;
    }

    private static class RankElement<T extends Individual> {
        T individual;
        double start;
        double size;

        RankElement(T individual, double start, double size) {
            this.individual = individual;
            this.start = start;
            this.size = size;
        }
    }
}
