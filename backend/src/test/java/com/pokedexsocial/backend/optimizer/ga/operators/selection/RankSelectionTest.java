package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RankSelection using white-box branch coverage.
 */
@ExtendWith(MockitoExtension.class)
class RankSelectionTest {

    @Mock
    private Random random;

    @InjectMocks
    private RankSelection<TestIndividual> rankSelection;

    @BeforeEach
    void setUp() {
        // Nothing to initialize explicitly; @InjectMocks creates the instance
    }

    @Test
        // Tests that the original population instance is returned unchanged when the population is empty
    void apply_ShouldReturnSamePopulation_WhenPopulationIsEmpty() throws CloneNotSupportedException {
        // Arrange
        TestPopulation population = new TestPopulation(10L); // empty population

        // Act
        Population<TestIndividual> result = rankSelection.apply(population, random);

        // Assert
        assertSame(population, result, "When population is empty, the same instance should be returned");
        verifyNoInteractions(random);
    }

    @Test
        // Tests that a new population is created with incremented id and cloned individuals when population is non-empty
    void apply_ShouldProduceClonedSelectedPopulationWithIncrementedId_WhenPopulationHasElements() throws CloneNotSupportedException {
        // Arrange
        TestPopulation population = new TestPopulation(5L);
        TestIndividual ind1 = new TestIndividual(1.0); // worst
        TestIndividual ind2 = new TestIndividual(2.0);
        TestIndividual ind3 = new TestIndividual(3.0); // best

        population.add(ind1);
        population.add(ind2);
        population.add(ind3);

        // Sorted by fitness ascending: [ind1 (1.0), ind2 (2.0), ind3 (3.0)]
        // Ranks: 1, 2, 3 -> totalRankSum = 6
        // Intervals:
        //  ind1: [0.0, 1/6)
        //  ind2: [1/6, 0.5)
        //  ind3: [0.5, 1.0)
        when(random.nextDouble()).thenReturn(
                0.0,   // select ind1
                0.2,   // select ind2
                0.75   // select ind3
        );

        long originalId = population.getId();

        // Act
        Population<TestIndividual> result = rankSelection.apply(population, random);

        // Assert
        assertNotSame(population, result, "Resulting population should be a different instance from original");
        assertEquals(population.size(), result.size(), "Resulting population should have same size as original");
        assertEquals(originalId + 1, result.getId(), "Resulting population id should be incremented by 1");
        assertEquals(originalId, population.getId(), "Original population id should remain unchanged");

        // Verify that Random was invoked once per individual
        verify(random, times(population.size())).nextDouble();

        // Collect fitness of selected individuals to ensure correct selection order
        List<Double> selectedFitness = new ArrayList<>();
        for (TestIndividual ind : result) {
            selectedFitness.add(ind.getFitness());
        }

        assertEquals(3, selectedFitness.size());
        assertEquals(1.0, selectedFitness.get(0));
        assertEquals(2.0, selectedFitness.get(1));
        assertEquals(3.0, selectedFitness.get(2));

        // Ensure that individuals in the new population are clones, not the same references as originals
        for (TestIndividual selected : result) {
            assertFalse(population.contains(selected),
                    "Cloned individuals should not be the same instance as any in the original population");
        }
    }

    @Test
        // Tests that boundary pointer values exactly on rank interval starts select the expected individuals
    void apply_ShouldSelectCorrectIndividuals_WhenPointerIsOnRankIntervalBoundaries() throws CloneNotSupportedException {
        // Arrange
        TestPopulation population = new TestPopulation(1L);
        TestIndividual ind1 = new TestIndividual(10.0); // worse
        TestIndividual ind2 = new TestIndividual(20.0); // better

        population.add(ind1);
        population.add(ind2);

        // Sorted ascending: [ind1 (10.0), ind2 (20.0)]
        // Ranks: 1, 2  -> totalRankSum = 3
        // Intervals:
        //  ind1: [0.0, 1/3)
        //  ind2: [1/3, 1.0)
        double boundary = 1.0 / 3.0;

        when(random.nextDouble()).thenReturn(
                0.0,      // exactly at start of first interval -> select ind1
                boundary  // exactly at start of second interval -> select ind2
        );

        // Act
        Population<TestIndividual> result = rankSelection.apply(population, random);

        // Assert
        assertEquals(2, result.size(), "Resulting population should have same size as original");

        List<Double> fitnesses = new ArrayList<>();
        for (TestIndividual ind : result) {
            fitnesses.add(ind.getFitness());
        }

        // First pointer (0.0) should map to first interval -> ind1
        // Second pointer (boundary) should map to second interval -> ind2
        assertEquals(10.0, fitnesses.get(0));
        assertEquals(20.0, fitnesses.get(1));

        verify(random, times(2)).nextDouble();
    }

    /**
     * Simple concrete Individual implementation used only for testing.
     */
    private static class TestIndividual extends Individual {

        private double fitness;

        TestIndividual(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }

        @Override
        public TestIndividual clone() throws CloneNotSupportedException {
            return (TestIndividual) super.clone();
        }
    }

    @Test
    // Tests the branch where no rank interval matches the pointer (if-condition always false)
    void apply_ShouldSkipAllRankElements_WhenPointerDoesNotMatchAnyInterval() throws CloneNotSupportedException {
        // Arrange
        TestPopulation population = new TestPopulation(7L);
        TestIndividual ind1 = new TestIndividual(1.0);
        TestIndividual ind2 = new TestIndividual(2.0);
        population.add(ind1);
        population.add(ind2);

        // Sorted: [ind1, ind2]
        // Intervals fully occupy [0,1)
        // So pointer = 1.0 is OUTSIDE → no match
        when(random.nextDouble()).thenReturn(1.0, 1.0);

        // Act
        Population<TestIndividual> result = rankSelection.apply(population, random);

        // Assert
        assertEquals(0, result.size(),
                "When pointer matches no rank interval for all iterations, resulting population must be empty");

        verify(random, times(population.size())).nextDouble();
    }

    @Test
    void apply_ShouldUseSortedOrderForRank_AssigningCorrectRankPositions() throws CloneNotSupportedException {
        // ----- Arrange -----
        TestPopulation population = new TestPopulation(99L);

        // Inserimento volontariamente NON ordinato
        TestIndividual indA = new TestIndividual(50.0); // should be rank 3 (best)
        TestIndividual indB = new TestIndividual(10.0); // should be rank 1 (worst)
        TestIndividual indC = new TestIndividual(30.0); // should be rank 2 (middle)

        // order of insertion: A, B, C
        population.add(indA);
        population.add(indB);
        population.add(indC);

        // Dopo SORT (fitness ascending) → B (10), C (30), A (50)
        //
        // ranks = 1, 2, 3  → totalRankSum = 6
        // intervals:
        //   B: [0.0, 1/6)
        //   C: [1/6, 3/6)
        //   A: [3/6, 1.0)

        // Pointer scelti apposta per selezionare ognuno in base al ranking SORTED
        when(random.nextDouble()).thenReturn(
                0.00,   // picks B
                0.30,   // picks C
                0.80    // picks A
        );

        // ----- Act -----
        Population<TestIndividual> result = rankSelection.apply(population, random);

        // ----- Assert -----
        List<Double> fitness = new ArrayList<>();
        for (TestIndividual t : result) fitness.add(t.getFitness());

        assertEquals(List.of(10.0, 30.0, 50.0), fitness,
                "Selection must follow SORTED rank order, not insertion order");

        // Verifica che sort() è effettivamente necessario
        // Senza sort → gli intervalli sarebbero costruiti su [A,B,C], non su [B,C,A]
        // e i pointer sceglierebbero individui sbagliati.
    }

    /**
     * Simple concrete Population implementation used only for testing.
     */
    private static class TestPopulation extends Population<TestIndividual> {

        TestPopulation(long id) {
            super(id);
        }
    }
}

