package com.pokedexsocial.backend.optimizer.ga.population;

import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

// Concrete stub implementation of Individual for testing
class TestIndividual extends Individual {
    private final double fitness;

    TestIndividual(double fitness) {
        this.fitness = fitness;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public Individual clone() {
        return new TestIndividual(fitness);
    }

    @Override
    public int compareTo(Individual o) {
        return Double.compare(this.fitness, o.getFitness());
    }
}

// Concrete subclass of Population for testing (Population is abstract)
class TestPopulation extends Population<TestIndividual> {
    public TestPopulation(long id) {
        super(id);
    }
}

@ExtendWith(MockitoExtension.class)
class PopulationTest {

    private TestPopulation population;

    @BeforeEach
    void setUp() {
        population = new TestPopulation(1L);
    }

    @Test
        // Tests that getId and setId work as expected
    void getId_ShouldReturnUpdatedValue_WhenSetIdCalled() {
        // Arrange
        population.setId(42L);

        // Act
        long result = population.getId();

        // Assert
        assertEquals(42L, result);
    }

    @Test
        // Tests that getBestIndividual and setBestIndividual store and retrieve correctly
    void setBestIndividual_ShouldStoreValue_WhenCalled() {
        // Arrange
        TestIndividual ind = new TestIndividual(10.0);

        // Act
        population.setBestIndividual(ind);

        // Assert
        assertSame(ind, population.getBestIndividual());
    }

    @Test
        // Tests average fitness when population is empty (edge case)
    void getAverageFitness_ShouldReturnZero_WhenPopulationIsEmpty() {
        // Act
        double result = population.getAverageFitness();

        // Assert
        assertEquals(0.0, result, 0.0001);
    }

    @Test
        // Tests average fitness calculation when population has individuals
    void getAverageFitness_ShouldReturnMeanFitness_WhenPopulationHasIndividuals() {
        // Arrange
        population.add(new TestIndividual(5.0));
        population.add(new TestIndividual(15.0));

        // Act
        double result = population.getAverageFitness();

        // Assert
        assertEquals(10.0, result, 0.0001);
    }

    @Test
        // Tests compareTo returns 0 when average fitness is equal
    void compareTo_ShouldReturnZero_WhenAverageFitnessIsEqual() {
        // Arrange
        TestPopulation other = new TestPopulation(2L);
        population.add(new TestIndividual(10.0));
        other.add(new TestIndividual(10.0));

        // Act
        int result = population.compareTo(other);

        // Assert
        assertEquals(0, result);
    }

    @Test
        // Tests compareTo returns negative when current population has lower average fitness
    void compareTo_ShouldReturnNegative_WhenThisPopulationHasLowerFitness() {
        // Arrange
        TestPopulation other = new TestPopulation(2L);
        population.add(new TestIndividual(5.0));
        other.add(new TestIndividual(10.0));

        // Act
        int result = population.compareTo(other);

        // Assert
        assertTrue(result < 0);
    }

    @Test
        // Tests compareTo returns positive when current population has higher average fitness
    void compareTo_ShouldReturnPositive_WhenThisPopulationHasHigherFitness() {
        // Arrange
        TestPopulation other = new TestPopulation(2L);
        population.add(new TestIndividual(20.0));
        other.add(new TestIndividual(10.0));

        // Act
        int result = population.compareTo(other);

        // Assert
        assertTrue(result > 0);
    }

    @Test
        // Tests that clone creates a shallow copy with same contents but different instance
    void clone_ShouldReturnNewPopulationInstanceWithSameElements() {
        // Arrange
        TestIndividual ind1 = new TestIndividual(7.0);
        population.add(ind1);

        // Act
        TestPopulation cloned = (TestPopulation) population.clone();

        // Assert
        assertNotSame(population, cloned);
        assertEquals(population, cloned);
        // Shallow copy: same element reference
        Iterator<TestIndividual> it1 = population.iterator();
        Iterator<TestIndividual> it2 = cloned.iterator();
        assertSame(it1.next(), it2.next());
    }

    @Test
        // Tests equals returns true for same instance
    void equals_ShouldReturnTrue_WhenSameInstance() {
        // Assert
        assertTrue(population.equals(population));
    }

    @Test
        // Tests equals returns false when comparing to null
    void equals_ShouldReturnFalse_WhenComparedWithNull() {
        // Assert
        assertFalse(population.equals(null));
    }

    @Test
        // Tests equals returns false when compared to different class
    void equals_ShouldReturnFalse_WhenComparedWithDifferentClass() {
        // Arrange
        Object other = "NotAPopulation";

        // Assert
        assertFalse(population.equals(other));
    }

    @Test
        // Tests equals returns false when same elements but different id
    void equals_ShouldReturnFalse_WhenIdsDiffer() {
        // Arrange
        TestPopulation other = new TestPopulation(99L);
        TestIndividual ind = new TestIndividual(10.0);
        population.add(ind);
        other.add(ind);

        // Assert
        assertFalse(population.equals(other));
    }

    @Test
        // Tests equals returns false when elements differ
    void equals_ShouldReturnFalse_WhenElementsDiffer() {
        // Arrange
        TestPopulation other = new TestPopulation(1L);
        population.add(new TestIndividual(10.0));
        other.add(new TestIndividual(20.0));

        // Assert
        assertFalse(population.equals(other));
    }

    @Test
        // Tests equals returns true when ids and elements are the same
    void equals_ShouldReturnTrue_WhenIdsAndElementsMatch() {
        // Arrange
        TestIndividual ind = new TestIndividual(10.0);
        population.add(ind);
        TestPopulation other = new TestPopulation(1L);
        other.add(ind);

        // Assert
        assertTrue(population.equals(other));
    }

    @Test
        // Tests hashCode consistency with equals
    void hashCode_ShouldBeEqual_WhenObjectsAreEqual() {
        // Arrange
        TestIndividual ind = new TestIndividual(10.0);
        population.add(ind);
        TestPopulation other = new TestPopulation(1L);
        other.add(ind);

        // Act
        int hash1 = population.hashCode();
        int hash2 = other.hashCode();

        // Assert
        assertEquals(hash1, hash2);
    }

    @Test
        // Tests hashCode differs when ids differ
    void hashCode_ShouldDiffer_WhenIdsDiffer() {
        // Arrange
        TestIndividual ind = new TestIndividual(10.0);
        population.add(ind);
        TestPopulation other = new TestPopulation(2L);
        other.add(ind);

        // Act
        int hash1 = population.hashCode();
        int hash2 = other.hashCode();

        // Assert
        assertNotEquals(hash1, hash2);
    }
}
