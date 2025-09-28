package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component("SinglePoint")
public class PokemonTeamSinglePointCrossover extends CrossoverOperator<PokemonTeamGA>{

    @Override
    public Population<PokemonTeamGA> apply(Population<PokemonTeamGA> population, Random rand) throws CloneNotSupportedException {


        Population<PokemonTeamGA> offsprings = population.clone();
        offsprings.setId(population.getId()+1);
        offsprings.clear();

        //Effettua il crossover
        List<Pairing> pairings = makeRandomPairings(population);
        for (Pairing pairing : pairings){
            PokemonGA[] firstCoding = pairing.firstParent.getCoding();
            PokemonGA[] secondCoding = pairing.secondParent.getCoding();

            int minLength = Math.min(firstCoding.length, secondCoding.length);
            int cutPoint = rand.nextInt(minLength-1) + 1;

            PokemonGA[] firstCodingLeft = Arrays.copyOfRange(firstCoding,0, cutPoint);
            PokemonGA[] firstCodingRight = Arrays.copyOfRange(firstCoding, cutPoint, minLength);
            PokemonGA[] secondCodingLeft = Arrays.copyOfRange(secondCoding,0, cutPoint);
            PokemonGA[] secondCodingRight = Arrays.copyOfRange(secondCoding, cutPoint, minLength);

            PokemonGA[] offspring1 = new PokemonGA[firstCodingLeft.length + secondCodingRight.length];
            System.arraycopy(firstCodingLeft,0, offspring1, 0, firstCodingLeft.length);
            System.arraycopy(secondCodingRight, 0, offspring1, firstCodingLeft.length, secondCodingRight.length);

            PokemonGA[] offspring2 = new PokemonGA[secondCodingLeft.length + firstCodingRight.length];
            System.arraycopy(secondCodingLeft,0, offspring2, 0, secondCodingLeft.length);
            System.arraycopy(firstCodingRight, 0, offspring2, secondCodingLeft.length, firstCodingRight.length);

            offsprings.add(new PokemonTeamGA(offspring1));
            offsprings.add(new PokemonTeamGA(offspring2));
        }
        return offsprings;
    }
}
