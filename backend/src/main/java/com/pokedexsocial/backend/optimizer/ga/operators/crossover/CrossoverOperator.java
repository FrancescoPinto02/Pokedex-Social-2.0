package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.operators.GeneticOperator;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.ga.utils.GAUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Classe astratta che definisce il comportamento base dell'operatore di crossover.
 *
 * @param <T> tipo di individuo (es. PokemonTeamGA)
 */
public abstract class CrossoverOperator<T extends Individual> extends GeneticOperator<T> {

    class Pairing {
        //@ spec_public
        protected final T firstParent;
        //@ spec_public
        protected final T secondParent;

        /*@
          @ normal_behavior
          @ requires firstParent != null;
          @ requires secondParent != null;
          @ ensures this.firstParent == firstParent;
          @ ensures this.secondParent == secondParent;
          @ pure
          @*/
        protected Pairing(T firstParent, T secondParent) {
            this.firstParent = firstParent;
            this.secondParent = secondParent;
        }
    }

    /*@
      @ normal_behavior
      @ requires population != null;
      @ requires population.id >= 0;
      @ requires population.size() > 0;
      @ requires (\forall T ind; population.contains(ind); ind != null && ind.fitness >= 0);
      @
      @ assignable \nothing;
      @
      @ ensures \result != null;
      @ ensures (\forall int i; 0 <= i && i < \result.size(); \result.get(i) != null && \result.get(i).firstParent != null && \result.get(i).secondParent != null);
      @
      @ // Se la popolazione ha un solo individuo il risultato deve avere una coppia
      @ ensures population.size() == 1 ==> \result.size() == 1;
      @
      @ // Se la popolazione ha due o più individui il risultato deve avere un numero
      @ // di coppie pari al numero di individui diviso 2
      @ ensures population.size() >= 2 ==> \result.size() == population.size() / 2;
      @
      @ // Il risultato deve contenere solo individui che appartengono anche alla popolazione
      @ ensures (\forall int i; 0 <= i && i < \result.size(); population.contains(\result.get(i).firstParent) && population.contains(\result.get(i).secondParent));
      @
      @ // Preserving Population invariants
      @ ensures population.id == \old(population.id);
      @ ensures population.size() == \old(population.size());
      @*/
    protected List<Pairing> makeRandomPairings(Population<T> population) {
        //@ assume population != null && population.id >= 0;
        //@ assume population.size() >= 0;
        List<Pairing> pairings = new ArrayList<>();
        final int originalSize = population.size();
        ArrayList<T> populationList = new ArrayList<>(population);
        //@ assume populationList.size() == originalSize;
        
        if (originalSize < 2) {
            //@ assert originalSize == 1;
            T loneIndividual = populationList.get(0);
            Pairing pairing = new Pairing(loneIndividual, loneIndividual);
            pairings.add(pairing);
            //@ assert pairings.size() == 1;
            //@ assert originalSize == 1 ==> pairings.size() == 1;
        } else {
            //@ assert originalSize >= 2;
            //@ assert populationList.size() == originalSize;
            
            // Collections.shuffle(populationList);
            int sizeToUse = populationList.size();
            if (sizeToUse % 2 != 0) {
                sizeToUse--;
            }

            //@ assert sizeToUse % 2 == 0;
            //@ assert sizeToUse <= populationList.size();
            //@ assume pairings.size() == 0;

            /*@
              @ loop_invariant i % 2 == 0;
              @ loop_invariant 0 <= i && i <= sizeToUse;
              @ loop_invariant pairings.size() == i / 2;
              @ loop_invariant (\forall int j; 0 <= j && j < pairings.size(); 
              @                population.contains(pairings.get(j).firstParent) && 
              @                population.contains(pairings.get(j).secondParent));
              @ decreases sizeToUse - i;
              @*/
            for (int i = 0; i < sizeToUse; i = i + 2) {
                T firstParent = populationList.get(i);
                T secondParent = populationList.get(i + 1);
                Pairing pairing = new Pairing(firstParent, secondParent);
                pairings.add(pairing);
            }
            //@ assert pairings.size() == sizeToUse / 2;
            //@ assert originalSize >= 2 ==> pairings.size() == originalSize / 2;
        }

        //@ show pairings.size();
        //@ assert (originalSize < 2 ==> pairings.size() == 1) && (originalSize >= 2 ==> pairings.size() == originalSize / 2);
        return pairings;
    }
}
