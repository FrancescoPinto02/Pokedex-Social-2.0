package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CrossoverOperatorTest {

    // Simple concrete Individual for testing
    static class TestIndividual extends Individual {
        private final String name;

        TestIndividual(String name) {
            this.name = name;
        }

        @Override
        public double getFitness() {
            return 0;
        }

        @Override
        public Individual clone() {
            return new TestIndividual(name);
        }

        @Override
        public int compareTo(Individual o) {
            return 0;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // Minimal concrete subclass to expose the protected method
    static class TestCrossoverOperator extends CrossoverOperator<TestIndividual> {
        @Override
        public Population<TestIndividual> apply(Population<TestIndividual> population, Random rand) {
            return population; // no-op
        }

        public List<Pairing> testMakeRandomPairings(Population<TestIndividual> pop) {
            return makeRandomPairings(pop);
        }
    }

    private TestCrossoverOperator crossover;

    @BeforeEach
    void setUp() {
        crossover = new TestCrossoverOperator();
    }

    @Test
        // Tests that makeRandomPairings throws IndexOutOfBoundsException for empty population
    void makeRandomPairings_ShouldThrowException_WhenPopulationIsEmpty() {
        // Arrange
        Population<TestIndividual> pop = new FixedSizePopulation<>(1L, 0);

        // Act & Assert
        assertThrows(IndexOutOfBoundsException.class, () -> crossover.testMakeRandomPairings(pop));
    }

    @Test
        // Tests that a single individual produces a self-self pairing
    void makeRandomPairings_ShouldReturnSelfPair_WhenSingleIndividual() {
        // Arrange
        Population<TestIndividual> pop = new FixedSizePopulation<>(1L, 1);
        TestIndividual ind = new TestIndividual("A");
        pop.add(ind);

        // Act
        List<CrossoverOperator<TestIndividual>.Pairing> pairings = crossover.testMakeRandomPairings(pop);

        // Assert
        assertEquals(1, pairings.size());
        CrossoverOperator<TestIndividual>.Pairing p = pairings.get(0);
        assertSame(ind, p.firstParent);
        assertSame(ind, p.secondParent);
    }

    @Test
    // Tests that two individuals produce exactly one pairing (A-B or B-A)
    void makeRandomPairings_ShouldReturnOnePair_WhenTwoIndividuals() {
        // Arrange
        Population<TestIndividual> pop = new FixedSizePopulation<>(1L, 2);
        TestIndividual ind1 = new TestIndividual("A");
        TestIndividual ind2 = new TestIndividual("B");
        pop.add(ind1);
        pop.add(ind2);

        // Act
        List<CrossoverOperator<TestIndividual>.Pairing> pairings = crossover.testMakeRandomPairings(pop);

        // Assert
        assertEquals(1, pairings.size(), "Population of two individuals should produce exactly one pairing");

        CrossoverOperator<TestIndividual>.Pairing p = pairings.get(0);

        // --- Conditional assertion logic ---
        // If first parent is ind1, then second must be ind2
        // If first parent is ind2, then second must be ind1
        // Otherwise, fail explicitly (should never happen)
        if (p.firstParent == ind1) {
            assertSame(ind2, p.secondParent, "If first parent is A, second must be B");
        } else if (p.firstParent == ind2) {
            assertSame(ind1, p.secondParent, "If first parent is B, second must be A");
        } else {
            fail("Unexpected parent in pairing: must be either A or B");
        }

        // --- Additional safety checks ---
        assertNotNull(p.firstParent, "First parent should not be null");
        assertNotNull(p.secondParent, "Second parent should not be null");
        assertNotSame(p.firstParent, p.secondParent, "Parents should be distinct individuals");
    }

    @Test
    // Tests that odd number of individuals ignores the last one
    void makeRandomPairings_ShouldIgnoreLast_WhenOddNumberGreaterThanTwo() {
        // Arrange
        Population<TestIndividual> pop = new FixedSizePopulation<>(1L, 3);
        TestIndividual ind1 = new TestIndividual("A");
        TestIndividual ind2 = new TestIndividual("B");
        TestIndividual ind3 = new TestIndividual("C");
        pop.add(ind1);
        pop.add(ind2);
        pop.add(ind3);

        // Act
        List<CrossoverOperator<TestIndividual>.Pairing> pairings = crossover.testMakeRandomPairings(pop);

        // Assert
        assertEquals(1, pairings.size(), "Last element should be ignored");
        CrossoverOperator<TestIndividual>.Pairing p = pairings.get(0);
        assertTrue(pop.contains(p.firstParent));
        assertTrue(pop.contains(p.secondParent));
        assertNotNull(p.firstParent);
        assertNotNull(p.secondParent);
        assertNotEquals(p.firstParent, p.secondParent, "Distinct individuals should be paired when possible");
    }

    @Test
    // Tests that even number greater than two creates multiple pairings
    void makeRandomPairings_ShouldReturnMultiplePairs_WhenEvenNumberGreaterThanTwo() {
        // Arrange
        Population<TestIndividual> pop = new FixedSizePopulation<>(1L, 4);
        TestIndividual ind1 = new TestIndividual("A");
        TestIndividual ind2 = new TestIndividual("B");
        TestIndividual ind3 = new TestIndividual("C");
        TestIndividual ind4 = new TestIndividual("D");
        pop.add(ind1);
        pop.add(ind2);
        pop.add(ind3);
        pop.add(ind4);

        // Act
        List<CrossoverOperator<TestIndividual>.Pairing> pairings = crossover.testMakeRandomPairings(pop);

        // Assert
        assertEquals(2, pairings.size());
        for (CrossoverOperator<TestIndividual>.Pairing p : pairings) {
            assertTrue(pop.contains(p.firstParent));
            assertTrue(pop.contains(p.secondParent));
            assertNotNull(p.firstParent);
            assertNotNull(p.secondParent);
        }
    }
}
