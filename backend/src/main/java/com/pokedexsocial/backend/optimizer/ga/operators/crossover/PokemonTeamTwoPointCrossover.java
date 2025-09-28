package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component("TwoPoint")
public class PokemonTeamTwoPointCrossover extends CrossoverOperator<PokemonTeamGA> {
    @Override
    public Population<PokemonTeamGA> apply(Population<PokemonTeamGA> population, Random rand) throws CloneNotSupportedException {
        Population<PokemonTeamGA> offsprings = population.clone();
        offsprings.setId(population.getId() + 1);
        offsprings.clear();

        // Effettua il crossover
        List<Pairing> pairings = makeRandomPairings(population);
        for (Pairing pairing : pairings) {
            PokemonGA[] firstCoding = pairing.firstParent.getCoding();
            PokemonGA[] secondCoding = pairing.secondParent.getCoding();

            int minLength = Math.min(firstCoding.length, secondCoding.length);

            int cutPoint1 = rand.nextInt(minLength);
            int cutPoint2 = rand.nextInt(minLength);

            // Assicurati che i punti di taglio siano diversi
            while (cutPoint1 == cutPoint2) {
                cutPoint2 = rand.nextInt(minLength);
            }

            int start = Math.min(cutPoint1, cutPoint2);
            int end = Math.max(cutPoint1, cutPoint2);

            PokemonGA[] offspring1 = new PokemonGA[minLength];
            PokemonGA[] offspring2 = new PokemonGA[minLength];

            // Copia le parti esterne non tagliate
            System.arraycopy(firstCoding, 0, offspring1, 0, start);
            System.arraycopy(secondCoding, 0, offspring2, 0, start);

            System.arraycopy(firstCoding, end, offspring1, end, minLength - end);
            System.arraycopy(secondCoding, end, offspring2, end, minLength - end);

            // Copia la parte centrale tra i punti di taglio (scambio)
            System.arraycopy(firstCoding, start, offspring2, start, end - start);
            System.arraycopy(secondCoding, start, offspring1, start, end - start);

            offsprings.add(new PokemonTeamGA(offspring1));
            offsprings.add(new PokemonTeamGA(offspring2));
        }

        return offsprings;
    }
}
