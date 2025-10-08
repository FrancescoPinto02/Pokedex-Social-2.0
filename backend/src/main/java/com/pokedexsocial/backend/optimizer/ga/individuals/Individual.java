package com.pokedexsocial.backend.optimizer.ga.individuals;

public abstract class Individual implements Comparable<Individual>, Cloneable{
    //@ spec_public
    protected double fitness;

    /*@
      @ // la fitness non può mai essere negativa
      @ public invariant fitness >= 0;
      @*/


    /*@ public normal_behavior
      @   ensures this.fitness == 0.0;
      @ pure
      @*/
    public Individual() {
        this.fitness = 0.0;
    }

    /*@ public normal_behavior
      @   requires fitness >= 0;
      @   ensures this.fitness == fitness;
      @ pure
      @*/
    public Individual(double fitness) {
        this.fitness = fitness;
    }

    /*@ public normal_behavior
      @   ensures \result == fitness;
      @   assignable \nothing;
      @*/
    public /*@ pure @*/ double getFitness() {
        return fitness;
    }

    /*@ public normal_behavior
      @   requires fitness >= 0;
      @   assignable this.fitness;
      @   ensures this.fitness == fitness;
      @*/
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /*@ also
      @ public normal_behavior
      @   requires other != null;
      @   assignable \nothing;
      @   ensures (\result == 0 && this.fitness == other.fitness) ||
      @           (\result < 0 && this.fitness < other.fitness) ||
      @           (\result > 0 && this.fitness > other.fitness);
      @*/
    @Override
    public /*@ pure @*/ int compareTo(Individual other) {
        if (this.fitness < other.fitness) return -1;
        else if (this.fitness > other.fitness) return 1;
        else return 0;
    }


    //@ skipesc
    @Override
    public Individual clone() throws CloneNotSupportedException {
        return (Individual) super.clone();
    }
}

