package com.pokedexsocial.backend.optimizer.ga.operators.crossover;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonTeamSinglePointCrossoverTest {

    @Mock
    private Random random;

    @InjectMocks
    private PokemonTeamSinglePointCrossover crossover;

    @BeforeEach
    void setUp() {
        // Nothing special; @InjectMocks will create the instance
    }

    private PokemonGA newPokemon(String label) {
        // Named mock just helps debugging if needed
        return mock(PokemonGA.class, label);
    }

    @Test
        // Tests that apply increments population id and does not modify the original population
    void apply_ShouldIncrementIdAndNotModifyOriginal_WhenCalled() throws CloneNotSupportedException {
        // Arrange
        long originalId = 5L;
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(originalId, 10);

        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");

        PokemonTeamGA parent = new PokemonTeamGA(new PokemonGA[]{p1, p2});
        population.add(parent);

        // cutPoint calculation: minLength = 2 -> minLength - 1 = 1 -> nextInt(1) returns 0 -> cutPoint = 1
        when(random.nextInt(1)).thenReturn(0);

        // Act
        Population<PokemonTeamGA> offsprings = crossover.apply(population, random);

        // Assert
        assertNotNull(offsprings);
        assertNotSame(population, offsprings, "Offspring population should be a different instance");
        assertEquals(originalId, population.getId(), "Original population id must remain unchanged");
        assertEquals(originalId + 1, offsprings.getId(), "Offspring population id must be incremented by 1");

        assertEquals(1, population.size(), "Original population size must remain unchanged");
        assertEquals(2, offsprings.size(), "Each parent should produce two offspring");
    }

    @Test
        // Tests that a single individual produces two identical offspring equal to the parent
    void apply_ShouldProduceTwoIdenticalOffspring_WhenSingleIndividual() throws CloneNotSupportedException {
        // Arrange
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 10);

        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonGA p3 = newPokemon("P3");
        PokemonGA p4 = newPokemon("P4");

        PokemonGA[] parentCoding = new PokemonGA[]{p1, p2, p3, p4};
        PokemonTeamGA parent = new PokemonTeamGA(parentCoding);
        population.add(parent);

        // minLength = 4 -> nextInt(3) in [0..2], choose 1 -> cutPoint = 2
        when(random.nextInt(3)).thenReturn(1);

        // Act
        Population<PokemonTeamGA> offsprings = crossover.apply(population, random);

        // Assert
        assertEquals(2, offsprings.size(), "Single parent should also produce two offspring");

        // Both offspring should have the same coding as the parent
        for (PokemonTeamGA child : offsprings) {
            assertArrayEquals(parentCoding, child.getCoding(), "Offspring coding should match parent coding");
        }
    }

    @Test
        // Tests that crossover recombines two parents correctly when they have the same length
    void apply_ShouldPerformSinglePointCrossover_WhenTwoParentsSameLength() throws CloneNotSupportedException {
        // Arrange
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 10);

        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonGA p3 = newPokemon("P3");
        PokemonGA p4 = newPokemon("P4");

        PokemonGA q1 = newPokemon("Q1");
        PokemonGA q2 = newPokemon("Q2");
        PokemonGA q3 = newPokemon("Q3");
        PokemonGA q4 = newPokemon("Q4");

        PokemonTeamGA parent1 = new PokemonTeamGA(new PokemonGA[]{p1, p2, p3, p4});
        PokemonTeamGA parent2 = new PokemonTeamGA(new PokemonGA[]{q1, q2, q3, q4});
        population.add(parent1);
        population.add(parent2);

        // minLength = 4 -> nextInt(3) in [0..2]
        // We want cutPoint = 2 -> nextInt(3) must return 1 (1 + 1 = 2)
        when(random.nextInt(3)).thenReturn(1);

        // Expected children:
        // offspring1 = [p1, p2, q3, q4]
        // offspring2 = [q1, q2, p3, p4]
        PokemonGA[] expected1 = new PokemonGA[]{p1, p2, q3, q4};
        PokemonGA[] expected2 = new PokemonGA[]{q1, q2, p3, p4};

        // Act
        Population<PokemonTeamGA> offsprings = crossover.apply(population, random);

        // Assert
        assertEquals(2, offsprings.size(), "Two parents should produce exactly two offspring");

        Iterator<PokemonTeamGA> it = offsprings.iterator();
        PokemonGA[] child1 = it.next().getCoding();
        PokemonGA[] child2 = it.next().getCoding();

        boolean combination1 =
                Arrays.equals(child1, expected1) && Arrays.equals(child2, expected2);
        boolean combination2 =
                Arrays.equals(child1, expected2) && Arrays.equals(child2, expected1);

        assertTrue(
                combination1 || combination2,
                "Offspring should be [P1,P2,Q3,Q4] and [Q1,Q2,P3,P4] in any order"
        );
    }

    @Test
        // Tests that crossover respects minLength when parents have different coding lengths
    void apply_ShouldUseMinLength_WhenParentsHaveDifferentLengths() throws CloneNotSupportedException {
        // Arrange
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 10);

        // Parent1 length 4
        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonGA p3 = newPokemon("P3");
        PokemonGA p4 = newPokemon("P4");

        // Parent2 length 2
        PokemonGA q1 = newPokemon("Q1");
        PokemonGA q2 = newPokemon("Q2");

        PokemonTeamGA parent1 = new PokemonTeamGA(new PokemonGA[]{p1, p2, p3, p4});
        PokemonTeamGA parent2 = new PokemonTeamGA(new PokemonGA[]{q1, q2});
        population.add(parent1);
        population.add(parent2);

        // minLength = 2 -> minLength - 1 = 1 -> nextInt(1) -> 0 -> cutPoint = 1
        when(random.nextInt(1)).thenReturn(0);

        // Expected:
        // firstCoding = [p1,p2,p3,p4], secondCoding = [q1,q2], minLength=2
        // firstLeft = [p1], firstRight = [p2]
        // secondLeft = [q1], secondRight = [q2]
        // offspring1 = [p1, q2]
        // offspring2 = [q1, p2]
        PokemonGA[] expected1 = new PokemonGA[]{p1, q2};
        PokemonGA[] expected2 = new PokemonGA[]{q1, p2};

        // Act
        Population<PokemonTeamGA> offsprings = crossover.apply(population, random);

        // Assert
        assertEquals(2, offsprings.size(), "Two parents should produce exactly two offspring");

        Iterator<PokemonTeamGA> it = offsprings.iterator();
        PokemonGA[] child1 = it.next().getCoding();
        PokemonGA[] child2 = it.next().getCoding();

        assertEquals(2, child1.length, "Offspring length should equal minLength");
        assertEquals(2, child2.length, "Offspring length should equal minLength");

        boolean combination1 =
                Arrays.equals(child1, expected1) && Arrays.equals(child2, expected2);
        boolean combination2 =
                Arrays.equals(child1, expected2) && Arrays.equals(child2, expected1);

        assertTrue(
                combination1 || combination2,
                "Offspring should be [P1,Q2] and [Q1,P2] in any order"
        );
    }
}
