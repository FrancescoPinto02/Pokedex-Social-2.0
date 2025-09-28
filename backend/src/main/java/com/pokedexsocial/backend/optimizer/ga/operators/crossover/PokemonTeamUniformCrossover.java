package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component("Uniform")
public class PokemonTeamUniformCrossover extends CrossoverOperator<PokemonTeamGA>{

    @Override
    public Population<PokemonTeamGA> apply(Population<PokemonTeamGA> population, Random rand) throws CloneNotSupportedException {
        Population<PokemonTeamGA> offsprings = population.clone();
        offsprings.setId(population.getId()+1);
        offsprings.clear();

        List<Pairing> pairings = makeRandomPairings(population);
        for(Pairing pairing : pairings){
            PokemonTeamGA offspring1 = crossover(pairing.firstParent, pairing.secondParent, rand);
            PokemonTeamGA offspring2 = crossover(pairing.secondParent, pairing.firstParent, rand);
            offsprings.add(offspring1);
            offsprings.add(offspring2);
        }

        return offsprings;
    }

    private PokemonTeamGA crossover(PokemonTeamGA parent1, PokemonTeamGA parent2, Random rand){
        PokemonGA[] genes1 = parent1.getCoding();
        PokemonGA[] genes2 = parent2.getCoding();

        int lenght = Math.min(genes1.length, genes2.length);
        PokemonGA[] offspringGene = new PokemonGA[lenght];

        //Seleziona l`i-esimo gene casualmente dai genitori
        for(int i=0; i<lenght; i++){
            if(rand.nextBoolean()){
                offspringGene[i] = genes1[i];
            }
            else{
                offspringGene[i] = genes2[i];
            }
        }

        return new PokemonTeamGA(offspringGene);
    }
}
