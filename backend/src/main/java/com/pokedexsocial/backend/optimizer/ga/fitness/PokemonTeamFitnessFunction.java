package com.pokedexsocial.backend.optimizer.ga.fitness;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component("pokemonTeamFitnessFunction")
public class PokemonTeamFitnessFunction extends FitnessFunction<PokemonTeamGA> {
    private static final double MIN_FITNESS = 0;
    private static final double MAX_FITNESS = 100;

    @Value("${pokemon.fitness.weights.low:0.5}")
    private double LOW_WEIGHT;

    @Value("${pokemon.fitness.weights.normal:1.0}")
    private double NORMAL_WEIGHT;

    @Value("${pokemon.fitness.weights.high:1.5}")
    private double HIGH_WEIGHT;

    public PokemonTeamFitnessFunction() {
        super(true);
    }

    @Override
    public void evaluate(PokemonTeamGA individual) {
        //I team con più di una megaevoluzione non sono validi
        if(megaEvolutionCount(individual) > 1){
            individual.setFitness(0);
        }
        else{
            double fitness = HIGH_WEIGHT * averageTeamStats(individual) + NORMAL_WEIGHT * typesDiversity(individual) + NORMAL_WEIGHT * teamResistances(individual)
                    + NORMAL_WEIGHT * legendaryCount(individual) + HIGH_WEIGHT * commonWeaknesses(individual);

            //fitness = normalizeFitness(fitness, 0, (LOW_WEIGHT*MAX_FITNESS*0)+(NORMAL_WEIGHT*MAX_FITNESS*3)+(HIGH_WEIGHT*MAX_FITNESS*2), MIN_FITNESS, MAX_FITNESS);
            individual.setFitness(fitness);
        }
    }

    //Calcola le statistiche medie totali del team
    private double averageTeamStats(PokemonTeamGA individual){
        double total = 0;
        int n = 0;
        for(PokemonGA p : individual.getCoding()){
            if(p.getTotal() > PokemonGA.MAX_TOTAL_STATS_STANDARD){
                total += 600; //se il pokemon è leggendario gli attribuisce un totale più basso
            }
            else{
                total += p.getTotal();
            }
            n++;
        }
        return normalizeFitness(total/n, PokemonGA.MIN_TOTAL_STATS, PokemonGA.MAX_TOTAL_STATS_STANDARD, MIN_FITNESS, MAX_FITNESS);
    }

    //Calcola il numero di tipi differenti all`interno del team
    private double typesDiversity(PokemonTeamGA individual){
        HashSet<PokemonTypeName> teamTypes = new HashSet<>();

        for(PokemonGA p : individual.getCoding()){

            teamTypes.add(p.getType1().getName());

            if(p.getType2().getName() != PokemonTypeName.UNDEFINED){
                teamTypes.add(p.getType2().getName());
            }
        }

        return normalizeFitness(teamTypes.size(), 1, 12, MIN_FITNESS, MAX_FITNESS);
    }


    //Calcola le resistenze del team
    private double teamResistances(PokemonTeamGA individual){
        HashSet<PokemonTypeName> teamResistances = new HashSet<>();
        int minTeamResistances = 1;
        int maxTeamResistances = 18;

        for(PokemonGA p : individual.getCoding()){
            teamResistances.addAll(p.getResistances());
        }

        return normalizeFitness(teamResistances.size(), minTeamResistances, maxTeamResistances, MIN_FITNESS, MAX_FITNESS);
    }

    //Calcola il numero di pokemon leggendari/mistici/paradoss all`interno del team
    private double legendaryCount(PokemonTeamGA individual){
        double count = 0;
        for(PokemonGA x : individual.getCoding()){
            PokemonRarity rarity = x.getRarity();
            if(rarity == PokemonRarity.LEGENDARY || rarity == PokemonRarity.MYTHICAL){
                count += 2;
            } else if (rarity == PokemonRarity.SUB_LEGENDARY || rarity == PokemonRarity.PARADOX) {
                count += 1;
            }
        }
        return normalizeFitness(count, 6, 0, MIN_FITNESS, MAX_FITNESS);
    }

    //Calcola il numero di megaevoluzioni all`interno del team
    private int megaEvolutionCount(PokemonTeamGA individual){
        int count = 0;
        for(PokemonGA p : individual.getCoding()){
            if(p.isMegaEvolution()){
                count++;
            }
        }
        return count;
    }

    //Calcola il numero di debolezze in comune dei pokemon all`interno del team
    private double commonWeaknesses(PokemonTeamGA individual){
        Map<PokemonTypeName, Integer> weaknessMap = new HashMap<>();
        for(PokemonTypeName type : PokemonTypeName.values()){
            weaknessMap.put(type,0);
        }

        for(PokemonGA p : individual.getCoding()){
            for(PokemonTypeName weakness : p.getWeaknesses()){
                weaknessMap.put(weakness, weaknessMap.get(weakness)+1);
            }
        }

        int total = 0;
        int num = 0;
        for(PokemonTypeName key : weaknessMap.keySet()){
            int value = weaknessMap.get(key);
            if(value>0){
                total += value;
                num++;
            }
        }

        return normalizeFitness(total/num, total, 1, MIN_FITNESS, MAX_FITNESS);
    }

    //Funzione per normalizzare i valori di fitness
    private double normalizeFitness(double x, double minX, double maxX, double minY, double maxY){
        double normalizedFitness = (x - minX) / (maxX - minX) * (maxY - minY) + minY;
        return Math.max(0, Math.min(100, normalizedFitness));
    }
}
