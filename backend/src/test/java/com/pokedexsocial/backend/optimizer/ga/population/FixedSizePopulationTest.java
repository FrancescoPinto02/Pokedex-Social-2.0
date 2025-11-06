package com.pokedexsocial.backend.optimizer.ga.population;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

// Simple concrete Individual stub for testing
class FixedTestIndividual extends Individual {
    private final double fitness;
    FixedTestIndividual(double fitness) { this.fitness = fitness; }
    @Override public double getFitness() { return fitness; }
    @Override public Individual clone() { return new FixedTestIndividual(fitness); }
    @Override public int compareTo(Individual o) { return Double.compare(fitness, o.getFitness()); }
}

@ExtendWith(MockitoExtension.class)
class FixedSizePopulationTest {

    private FixedSizePopulation<FixedTestIndividual> population;

    @BeforeEach
    void setUp() {
        population = new FixedSizePopulation<>(1L, 3);
    }

    @Test
        // Tests that constructor stores positive maxSize unchanged
    void constructor_ShouldSetMaxSize_WhenPositiveValueProvided() {
        // Arrange & Act
        FixedSizePopulation<FixedTestIndividual> pop = new FixedSizePopulation<>(1L, 5);

        // Assert
        assertEquals(5, pop.getMaxSize());
    }

    @Test
        // Tests that constructor clamps negative maxSize to zero
    void constructor_ShouldClampMaxSizeToZero_WhenNegativeValueProvided() {
        // Arrange & Act
        FixedSizePopulation<FixedTestIndividual> pop = new FixedSizePopulation<>(1L, -10);

        // Assert
        assertEquals(0, pop.getMaxSize());
    }

    @Test
        // Tests that add works when population size is below maxSize
    void add_ShouldAddIndividual_WhenBelowMaxSize() {
        // Arrange
        FixedTestIndividual ind = new FixedTestIndividual(10.0);

        // Act
        boolean added = population.add(ind);

        // Assert
        assertTrue(added);
        assertTrue(population.contains(ind));
    }

    @Test
        // Tests that add returns false when population has reached maxSize
    void add_ShouldReturnFalse_WhenAtMaxSizeLimit() {
        // Arrange
        for (int i = 0; i < 3; i++) {
            population.add(new FixedTestIndividual(i));
        }
        FixedTestIndividual extra = new FixedTestIndividual(99.0);

        // Act
        boolean added = population.add(extra);

        // Assert
        assertFalse(added);
        assertFalse(population.contains(extra));
    }

    @Test
        // Tests that add allows unlimited additions when maxSize is zero
    void add_ShouldAllowUnlimitedAdditions_WhenMaxSizeIsZero() {
        // Arrange
        FixedSizePopulation<FixedTestIndividual> unlimited = new FixedSizePopulation<>(1L, 0);

        // Act
        for (int i = 0; i < 100; i++) {
            assertTrue(unlimited.add(new FixedTestIndividual(i)));
        }

        // Assert
        assertEquals(100, unlimited.size());
    }

    @Test
        // Tests that add allows unlimited additions when maxSize was negative (clamped to zero)
    void add_ShouldAllowUnlimitedAdditions_WhenMaxSizeWasNegative() {
        // Arrange
        FixedSizePopulation<FixedTestIndividual> unlimited = new FixedSizePopulation<>(1L, -1);

        // Act
        boolean added = unlimited.add(new FixedTestIndividual(5.0));

        // Assert
        assertTrue(added);
        assertEquals(1, unlimited.size());
    }

    @Test
        // Tests getMaxSize returns stored value
    void getMaxSize_ShouldReturnConfiguredValue() {
        // Assert
        assertEquals(3, population.getMaxSize());
    }

    @Test
        // Tests that add does not add duplicates (inherited HashSet behavior)
    void add_ShouldNotAddDuplicateIndividuals() {
        // Arrange
        FixedTestIndividual ind = new FixedTestIndividual(10.0);
        population.add(ind);

        // Act
        boolean result = population.add(ind);

        // Assert
        assertFalse(result, "HashSet should not allow duplicates");
        assertEquals(1, population.size());
    }
}

