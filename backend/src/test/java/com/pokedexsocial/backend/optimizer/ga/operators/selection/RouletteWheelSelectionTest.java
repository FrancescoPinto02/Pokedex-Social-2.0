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
    void apply_ShouldNotSelectElement_WhenPointerIsExactlyAtRightBoundary() throws CloneNotSupportedException {

        // Popolazione con fitness differenti
        Population<Individual> population = new FixedSizePopulation<>(4L, 2);
        TestIndividual a = new TestIndividual("A", 1.0); // 25% area: [0.0, 0.25)
        TestIndividual b = new TestIndividual("B", 3.0); // 75% area: [0.25, 1.0)
        population.add(a);
        population.add(b);

        // Pointer in EXACT right boundary of A → should NOT select A
        when(random.nextDouble()).thenReturn(0.25);

        Population<Individual> result = selectionOperator.apply(population, random);
        Individual selected = result.iterator().next();

        assertEquals("B", selected.toString(),
                "Pointer equal to right boundary must NOT select left element");
    }


    @Test
    void apply_ShouldNotSelectElement_WhenPointerEqualsRightBoundary() throws CloneNotSupportedException {
        // arrange
        RouletteWheelSelection<Individual> sel = new RouletteWheelSelection<>();
        Random rand = mock(Random.class);

        // Popolazione con due elementi
        Population<Individual> population = new FixedSizePopulation<>(1L, 2);

        Individual a = new TestIndividual("A", 1.0); // interval: [0.0, 0.25)
        Individual b = new TestIndividual("B", 3.0); // interval: [0.25, 1.0)

        population.add(a);
        population.add(b);

        // Pointer esattamente sul right-boundary di A
        when(rand.nextDouble()).thenReturn(0.25);

        // act
        Population<Individual> result = sel.apply(population, rand);

        // assert
        // la ruota ha stessa dimensione della popolazione → il primo estratto deve essere B
        Individual selected = result.iterator().next();

        assertEquals("B", selected.toString(),
                "Pointer on right boundary must NOT select left interval but the next interval");
    }

    @Test
    void apply_ShouldPerformExactlyOneSpinPerIndividual() throws CloneNotSupportedException {

        // Arrange: create 3 individuals with non-zero fitness
        Population<Individual> population = new FixedSizePopulation<>(10L, 3);

        Individual a = new TestIndividual("A", 1.0);
        Individual b = new TestIndividual("B", 1.0);
        Individual c = new TestIndividual("C", 1.0);

        population.add(a);
        population.add(b);
        population.add(c);

        // We force nextDouble() to always fall in the first interval
        when(random.nextDouble()).thenReturn(0.0, 0.0, 0.0);

        // Act
        Population<Individual> result = selectionOperator.apply(population, random);

        // Assert
        // The roulette wheel must be spun EXACTLY once per individual => 3 times
        assertEquals(3, result.size(),
                "Roulette wheel must generate exactly one selected individual per element in original population");
    }

    @Test
    void apply_ShouldIncrementPopulationIdByOne() throws CloneNotSupportedException {

        Population<Individual> population = new FixedSizePopulation<>(4L, 1);
        population.add(new TestIndividual("A", 1.0));

        when(random.nextDouble()).thenReturn(0.0);

        Population<Individual> result = selectionOperator.apply(population, random);

        assertEquals(5L, result.getId(),
                "Population ID must increment by exactly 1");
    }


}


