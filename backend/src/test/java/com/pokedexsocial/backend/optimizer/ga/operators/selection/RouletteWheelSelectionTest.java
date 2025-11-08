package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouletteWheelSelectionTest {

    private RouletteWheelSelection<Individual> selectionOperator;
    private Random random;

    static class TestIndividual extends Individual {
        private final double fitness;
        private final String name;

        TestIndividual(String name, double fitness) {
            this.name = name;
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }

        @Override
        public Individual clone() {
            return new TestIndividual(name, fitness);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        selectionOperator = new RouletteWheelSelection<>();
        random = mock(Random.class);
    }

    @Test
        // Verifies that totalFitness=0 returns the same population reference
    void apply_ShouldReturnSamePopulation_WhenTotalFitnessIsZero() throws CloneNotSupportedException {
        Population<Individual> population = new FixedSizePopulation<>(1L, 3);
        population.add(new TestIndividual("A", 0.0));
        population.add(new TestIndividual("B", 0.0));

        Population<Individual> result = selectionOperator.apply(population, random);

        assertSame(population, result, "When total fitness is zero, should return original population");
    }

    @Test
        // Verifies roulette selection works proportionally to fitness
    void apply_ShouldSelectBasedOnFitnessProportion_WhenFitnessesAreDifferent() throws CloneNotSupportedException {
        Population<Individual> population = new FixedSizePopulation<>(5L, 3);
        TestIndividual a = new TestIndividual("A", 1.0);
        TestIndividual b = new TestIndividual("B", 2.0);
        TestIndividual c = new TestIndividual("C", 3.0);
        population.add(a);
        population.add(b);
        population.add(c);

        when(random.nextDouble()).thenReturn(0.1, 0.3, 0.8);

        Population<Individual> result = selectionOperator.apply(population, random);

        assertEquals(3, result.size());
        assertEquals(6L, result.getId()); // ID increments
        Set<String> names = result.stream().map(Object::toString).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("A", "B", "C")));
    }

    @Test
        // Verifies each individual is cloned, not reused
    void apply_ShouldCloneIndividualsAndNotReuseOriginals() throws CloneNotSupportedException {
        Population<Individual> population = new FixedSizePopulation<>(9L, 2);
        TestIndividual a = new TestIndividual("A", 1.0);
        TestIndividual b = new TestIndividual("B", 1.0);
        population.add(a);
        population.add(b);

        when(random.nextDouble()).thenReturn(0.25, 0.75);

        Population<Individual> result = selectionOperator.apply(population, random);
        List<Individual> resultList = new ArrayList<>(result);

        assertEquals(2, resultList.size());
        for (Individual ind : resultList) {
            assertFalse(Set.of(a, b).contains(ind), "Result individuals should be cloned, not same references");
        }
    }

    @Test
        // Verifies selection handles exact 0 boundary correctly
    void apply_ShouldHandlePointerAtExactBoundary() throws CloneNotSupportedException {
        Population<Individual> population = new FixedSizePopulation<>(2L, 2);
        TestIndividual a = new TestIndividual("A", 1.0);
        TestIndividual b = new TestIndividual("B", 3.0);
        population.add(a);
        population.add(b);

        when(random.nextDouble()).thenReturn(0.0, 0.25, 0.99);

        Population<Individual> result = selectionOperator.apply(population, random);
        List<Individual> resultList = new ArrayList<>(result);

        assertEquals(2, resultList.size());
        assertTrue(resultList.stream().anyMatch(i -> i.toString().equals("A")),
                "At pointer 0.0, first element should be chosen");
    }
}


