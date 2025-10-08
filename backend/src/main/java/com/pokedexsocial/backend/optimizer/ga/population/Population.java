package com.pokedexsocial.backend.optimizer.ga.population;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;

import java.util.HashSet;
import java.util.Objects;

public abstract class Population<T extends Individual> extends HashSet<T> implements Comparable<Population<T>>{

    //@ spec_public
    private long id;
    //@ spec_public
    private /*@ nullable @*/ T bestIndividual;

    /*@
      @ public invariant id >= 0;
      @ public invariant this.size() >= 0;
      @ public invariant (\forall T ind; this.contains(ind); ind != null && ind.fitness>=0);
      @*/

    /*@ public normal_behavior
      @   requires id >= 0;
      @   ensures this.id == id;
      @   ensures this.bestIndividual == null;
      @   ensures this.isEmpty();
      @ pure
      @*/
    public Population(long id) {
        this.id = id;
        this.bestIndividual = null;
    }

    /*@ public normal_behavior
      @   ensures \result == id;
      @   assignable \nothing;
      @*/
    public /*@ pure @*/ long getId() {
        return id;
    }

    /*@ public normal_behavior
      @   requires id >= 0;
      @   assignable this.id;
      @   ensures this.id == id;
      @*/
    public void setId(long id) {
        this.id = id;
    }

    /*@ public normal_behavior
      @   ensures \result == bestIndividual;
      @   assignable \nothing;
      @*/
    public /*@ pure nullable @*/ T getBestIndividual() {
        return bestIndividual;
    }

    /*@ public normal_behavior
      @   requires bestIndividual != null;
      @   assignable this.bestIndividual;
      @   ensures this.bestIndividual == bestIndividual;
      @*/
    public void setBestIndividual(T bestIndividual) {
        this.bestIndividual = bestIndividual;
    }


    /*@ public normal_behavior
      @   assignable \nothing;
      @   requires this.size() <= Integer.MAX_VALUE;
      @   requires (\forall T ind; this.contains(ind); ind != null && ind.fitness >= 0);
      @
      @
      @   //Media 0 se la popolazione è vuota
      @   ensures this.isEmpty() ==> \result == 0.0;
      @
      @   //Media Positiva se la popolazione non è vuota
      @   ensures !this.isEmpty() ==> \result >= 0.0;
      @
      @*/
    public /*@ pure @*/ double getAverageFitness() {
        int size = this.size();
        double sum = 0.0;

        //@ maintaining 0 <= \count <= size;
        //@ maintaining sum >= 0.0;
        //@ maintaining \forall T x; this.contains(x); x != null && x.fitness >= 0;
        //@ loop_writes sum;
        //@ decreases size - \count;
        for (T ind : this) {
            sum += ind.getFitness();
        }

        return (size == 0) ? 0.0 : sum / size;
    }

    //@ skipesc
    @Override
    public int compareTo(Population other) {
        return Double.compare(this.getAverageFitness(), other.getAverageFitness());
    }

    //@ skipesc
    @Override
    public Population<T> clone() {
        return (Population<T>) super.clone();
    }


    //@ skipesc
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Population<?> that = (Population<?>) o;
        return id == that.id;
    }

    //@ skipesc
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
