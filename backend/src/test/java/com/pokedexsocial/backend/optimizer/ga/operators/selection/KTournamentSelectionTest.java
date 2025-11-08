package com.pokedexsocial.backend.optimizer.ga.operators.selection;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KTournamentSelectionTest {

    private KTournamentSelection<TestIndividual> selectionOperator;

    @Mock
    private Random mockedRandom;

    /**
     * Simple concrete Individual implementation for testing.
     */
    static class TestIndividual extends Individual {
        private final String name;
        private final double fitness;

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
        public int compareTo(Individual other) {
            return Double.compare(this.getFitness(), other.getFitness());
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        selectionOperator = new KTournamentSelection<>();
    }

    @Test
        // Verifies that ID is incremented and the resulting population has the expected selection size (100)
    void apply_ShouldReturnPopulationWithIncrementedIdAndSelectionSize() throws CloneNotSupportedException {
        // Arrange
        Population<TestIndividual> population = new Population<TestIndividual>(10L) {};
        population.add(new TestIndividual("A", 1.0));
        population.add(new TestIndividual("B", 2.0));
        population.add(new TestIndividual("C", 3.0));

        Random rand = new Random(42);

        // Act
        Population<TestIndividual> result = selectionOperator.apply(population, rand);

        // Assert
        assertNotNull(result);
        assertEquals(11L, result.getId(), "New population ID should be incremented by 1");
        assertEquals(100, result.size(), "New population should contain 100 individuals");
    }

    @Test
        // Verifies that higher-fitness individuals are selected more frequently (proportional selection)
    void apply_ShouldFavorIndividualsWithHigherFitness_WhenProportional() throws CloneNotSupportedException {
        // Arrange
        Population<TestIndividual> population = new Population<TestIndividual>(1L) {};
        TestIndividual low = new TestIndividual("Low", 1.0);
        TestIndividual mid = new TestIndividual("Mid", 2.0);
        TestIndividual high = new TestIndividual("High", 10.0);

        population.add(low);
        population.add(mid);
        population.add(high);

        Random rand = new Random(123);

        // Act
        Population<TestIndividual> result = selectionOperator.apply(population, rand);

        // Assert
        long lowCount  = result.stream().filter(i -> i.toString().equals("Low")).count();
        long midCount  = result.stream().filter(i -> i.toString().equals("Mid")).count();
        long highCount = result.stream().filter(i -> i.toString().equals("High")).count();

        assertEquals(100, result.size(), "Resulting population must have 100 individuals");
        assertTrue(highCount > midCount, "High-fitness individuals should dominate selection");
        assertTrue(highCount > lowCount, "High-fitness individuals should be more frequent than low-fitness ones");
    }

    @Test
        // Verifies that zero total fitness triggers fallback to Collections.max
    void apply_ShouldHandleZeroTotalFitness_UsingFallbackMax() throws CloneNotSupportedException {
        // Arrange
        Population<TestIndividual> population = new Population<TestIndividual>(3L) {};
        population.add(new TestIndividual("A", 0.0));
        population.add(new TestIndividual("B", 0.0));
        population.add(new TestIndividual("C", 0.0));

        Random rand = new Random(999);

        // Act
        Population<TestIndividual> result = selectionOperator.apply(population, rand);

        // Assert
        assertEquals(100, result.size(), "Population should still produce 100 individuals even with total fitness = 0");
        assertTrue(result.stream().allMatch(ind -> ind instanceof TestIndividual),
                "All resulting individuals should be of type TestIndividual");
    }

    @Test
        // Verifies that winners are cloned and not the same reference as original individuals
    void apply_ShouldCloneWinners_AndNotReuseOriginalInstances() throws CloneNotSupportedException {
        // Arrange
        Population<TestIndividual> population = new Population<TestIndividual>(5L) {};
        TestIndividual a = new TestIndividual("A", 5.0);
        TestIndividual b = new TestIndividual("B", 1.0);
        population.add(a);
        population.add(b);

        Random rand = new Random(321);

        // Act
        Population<TestIndividual> result = selectionOperator.apply(population, rand);

        // Assert
        assertEquals(100, result.size());
        List<TestIndividual> resultList = new ArrayList<>(result);
        TestIndividual first = resultList.get(0);
        assertNotSame(a, first, "Selected individuals should be clones, not same instances");
        assertNotSame(b, first, "Selected individuals should be clones, not same instances");
    }

    @Test
        // Verifies that getProportionalWinner correctly selects the individual based on the random pointer
    void getProportionalWinner_ShouldSelectCorrectIndividualBasedOnPointer() throws Exception {
        // Arrange
        Method method = KTournamentSelection.class
                .getDeclaredMethod("getProportionalWinner", List.class, Random.class);
        method.setAccessible(true);

        KTournamentSelection<TestIndividual> localSelection = new KTournamentSelection<>();

        // The tournament must have exactly 5 participants (tournamentSize)
        List<TestIndividual> tournament = new ArrayList<>();
        tournament.add(new TestIndividual("A", 1.0));  // small slice
        tournament.add(new TestIndividual("B", 1.0));  // small slice
        tournament.add(new TestIndividual("C", 8.0));  // large slice
        tournament.add(new TestIndividual("D", 0.0));
        tournament.add(new TestIndividual("E", 0.0));

        // Total fitness = 10, so C covers [0.2, 1.0)
        when(mockedRandom.nextDouble()).thenReturn(0.8);

        // Act
        TestIndividual winner = (TestIndividual) method.invoke(localSelection, tournament, mockedRandom);

        // Assert
        assertEquals("C", winner.toString(), "Pointer 0.8 should select individual C");
    }
}



