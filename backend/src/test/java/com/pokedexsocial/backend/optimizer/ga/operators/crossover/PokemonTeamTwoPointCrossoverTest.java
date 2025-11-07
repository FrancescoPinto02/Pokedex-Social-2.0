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
class PokemonTeamTwoPointCrossoverTest {

    @Mock
    private Random random;

    @InjectMocks
    private PokemonTeamTwoPointCrossover crossover;

    private PokemonGA newPokemon(String name) {
        return mock(PokemonGA.class, name);
    }

    @BeforeEach
    void resetMocks() {
        clearInvocations(random);
    }

    @Test
        // Verifies cloning and id increment without modifying the original population
    void apply_ShouldIncrementIdAndKeepOriginalUnchanged() throws CloneNotSupportedException {
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(7L, 5);
        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        population.add(new PokemonTeamGA(new PokemonGA[]{p1, p2}));

        // Any valid cut points
        when(random.nextInt(anyInt())).thenReturn(0, 1);

        Population<PokemonTeamGA> result = crossover.apply(population, random);

        assertNotNull(result);
        assertNotSame(population, result);
        assertEquals(7L, population.getId(), "Original id must remain the same");
        assertEquals(8L, result.getId(), "Offspring id should be incremented");
        assertEquals(1, population.size(), "Original population unchanged");
        assertEquals(2, result.size(), "Each pairing produces two offspring");
    }

    @Test
        // Tests correct two-point recombination when cut points are distinct
    void apply_ShouldPerformTwoPointCrossover_WhenCutPointsDistinct() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA a1 = newPokemon("A1");
        PokemonGA a2 = newPokemon("A2");
        PokemonGA a3 = newPokemon("A3");
        PokemonGA a4 = newPokemon("A4");

        PokemonGA b1 = newPokemon("B1");
        PokemonGA b2 = newPokemon("B2");
        PokemonGA b3 = newPokemon("B3");
        PokemonGA b4 = newPokemon("B4");

        PokemonTeamGA parent1 = new PokemonTeamGA(new PokemonGA[]{a1, a2, a3, a4});
        PokemonTeamGA parent2 = new PokemonTeamGA(new PokemonGA[]{b1, b2, b3, b4});
        pop.add(parent1);
        pop.add(parent2);

        // minLength = 4; cutPoint1=1, cutPoint2=3 => start=1, end=3
        when(random.nextInt(4)).thenReturn(1, 3);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());

        PokemonGA[] expectedChild1 = {a1, b2, b3, a4};
        PokemonGA[] expectedChild2 = {b1, a2, a3, b4};

        Iterator<PokemonTeamGA> it = children.iterator();
        PokemonGA[] c1 = it.next().getCoding();
        PokemonGA[] c2 = it.next().getCoding();

        boolean ok1 = Arrays.equals(c1, expectedChild1) && Arrays.equals(c2, expectedChild2);
        boolean ok2 = Arrays.equals(c1, expectedChild2) && Arrays.equals(c2, expectedChild1);
        assertTrue(ok1 || ok2, "Offspring must exchange middle segment between cut points");
    }

    @Test
        // Ensures the loop executes when cutPoint1 == cutPoint2 and new value differs
    void apply_ShouldRetryRandom_WhenCutPointsEqual() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

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
        pop.add(parent1);
        pop.add(parent2);

        // First call: 2, second call: 2 (equal), loop -> third call: 1 (different)
        when(random.nextInt(4)).thenReturn(2, 2, 1);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size(), "Should still produce two offspring");

        // Verify loop happened (3 calls to nextInt)
        verify(random, times(3)).nextInt(4);
    }

    @Test
        // Tests crossover uses minLength when parents have different coding lengths
    void apply_ShouldRespectMinLength_WhenParentsDifferentLengths() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA a1 = newPokemon("A1");
        PokemonGA a2 = newPokemon("A2");
        PokemonGA a3 = newPokemon("A3");
        PokemonGA a4 = newPokemon("A4");

        PokemonGA b1 = newPokemon("B1");
        PokemonGA b2 = newPokemon("B2");

        PokemonTeamGA longParent = new PokemonTeamGA(new PokemonGA[]{a1, a2, a3, a4});
        PokemonTeamGA shortParent = new PokemonTeamGA(new PokemonGA[]{b1, b2});
        pop.add(longParent);
        pop.add(shortParent);

        // minLength = 2; cutPoint1=0, cutPoint2=1 -> start=0, end=1
        when(random.nextInt(2)).thenReturn(0, 1);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());

        Iterator<PokemonTeamGA> it = children.iterator();
        PokemonGA[] c1 = it.next().getCoding();
        PokemonGA[] c2 = it.next().getCoding();

        assertEquals(2, c1.length);
        assertEquals(2, c2.length);

        // Expected child1 = [b1, a2]; child2 = [a1, b2]
        PokemonGA[] expected1 = {b1, a2};
        PokemonGA[] expected2 = {a1, b2};

        boolean ok1 = Arrays.equals(c1, expected1) && Arrays.equals(c2, expected2);
        boolean ok2 = Arrays.equals(c1, expected2) && Arrays.equals(c2, expected1);
        assertTrue(ok1 || ok2, "Children must use only minLength elements");
    }

    @Test
        // Single individual self-pair produces two identical offspring
    void apply_ShouldProduceTwoIdenticalOffspring_WhenSingleIndividual() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);
        PokemonGA g1 = newPokemon("G1");
        PokemonGA g2 = newPokemon("G2");
        PokemonGA g3 = newPokemon("G3");
        PokemonGA g4 = newPokemon("G4");

        PokemonTeamGA single = new PokemonTeamGA(new PokemonGA[]{g1, g2, g3, g4});
        pop.add(single);

        when(random.nextInt(4)).thenReturn(1, 3);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());
        for (PokemonTeamGA c : children) {
            assertArrayEquals(single.getCoding(), c.getCoding(), "Self-pair should reproduce identical codings");
        }
    }
}

