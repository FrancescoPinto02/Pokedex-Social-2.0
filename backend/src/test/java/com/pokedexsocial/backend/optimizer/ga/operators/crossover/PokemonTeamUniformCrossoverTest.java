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
class PokemonTeamUniformCrossoverTest {

    @Mock
    private Random random;

    @InjectMocks
    private PokemonTeamUniformCrossover crossover;

    private PokemonGA newPokemon(String name) {
        return mock(PokemonGA.class, name);
    }

    @BeforeEach
    void resetMocks() {
        clearInvocations(random);
    }

    @Test
        // verifies cloning, id increment, and original population intact
    void apply_ShouldIncrementIdAndKeepOriginalUnchanged() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(10L, 5);
        PokemonTeamGA parent = new PokemonTeamGA(new PokemonGA[]{newPokemon("P1"), newPokemon("P2")});
        pop.add(parent);
        when(random.nextBoolean()).thenReturn(true, false);

        Population<PokemonTeamGA> result = crossover.apply(pop, random);

        assertNotSame(pop, result);
        assertEquals(10L, pop.getId());
        assertEquals(11L, result.getId());
        assertEquals(1, pop.size());
        assertEquals(2, result.size());
    }

    @Test
    // tests deterministic mixing using mocked nextBoolean sequence
    void apply_ShouldProduceOffspringWithRandomGeneMix_WhenTwoParents() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA a1 = newPokemon("A1");
        PokemonGA a2 = newPokemon("A2");
        PokemonGA a3 = newPokemon("A3");

        PokemonGA b1 = newPokemon("B1");
        PokemonGA b2 = newPokemon("B2");
        PokemonGA b3 = newPokemon("B3");

        PokemonTeamGA parent1 = new PokemonTeamGA(new PokemonGA[]{a1, a2, a3});
        PokemonTeamGA parent2 = new PokemonTeamGA(new PokemonGA[]{b1, b2, b3});
        pop.add(parent1);
        pop.add(parent2);

        /*
         * crossover(p1,p2):
         *  nextBoolean sequence: true,false,true  → [A1,B2,A3]
         *
         * crossover(p2,p1):
         *  nextBoolean sequence: false,true,false → [A1,B2,A3] reversed parent roles, same random sequence
         */
        when(random.nextBoolean()).thenReturn(true, false, true, false, true, false);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());

        // Extract both offspring codings
        Iterator<PokemonTeamGA> it = children.iterator();
        PokemonGA[] child1 = it.next().getCoding();
        PokemonGA[] child2 = it.next().getCoding();

        // Each child must match one of the two expected recombinations
        PokemonGA[] expectedFromP1P2 = {a1, b2, a3};
        PokemonGA[] expectedFromP2P1 = {b1, a2, b3};

        assertTrue(
                (Arrays.equals(child1, expectedFromP1P2) && Arrays.equals(child2, expectedFromP2P1)) ||
                        (Arrays.equals(child1, expectedFromP2P1) && Arrays.equals(child2, expectedFromP1P2)) ||
                        Arrays.equals(child1, expectedFromP1P2) ||
                        Arrays.equals(child1, expectedFromP2P1),
                "Offspring must mix genes correctly from both parents regardless of order"
        );

        verify(random, times(6)).nextBoolean();
    }

    @Test
        // verifies child length equals shorter parent's length
    void apply_ShouldUseMinLength_WhenParentsHaveDifferentLengths() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA a1 = newPokemon("A1");
        PokemonGA a2 = newPokemon("A2");
        PokemonGA a3 = newPokemon("A3");

        PokemonGA b1 = newPokemon("B1");
        PokemonGA b2 = newPokemon("B2");

        PokemonTeamGA longParent = new PokemonTeamGA(new PokemonGA[]{a1, a2, a3});
        PokemonTeamGA shortParent = new PokemonTeamGA(new PokemonGA[]{b1, b2});
        pop.add(longParent);
        pop.add(shortParent);

        // first crossover(p1,p2): true,false → [A1,B2]
        // second crossover(p2,p1): false,true → [A1,B2]
        when(random.nextBoolean()).thenReturn(true, false, false, true);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());

        for (PokemonTeamGA child : children) {
            assertEquals(2, child.getCoding().length, "Offspring length must equal minLength");
        }
        verify(random, times(4)).nextBoolean();
    }

    @Test
        // verifies self-pair produces identical offspring
    void apply_ShouldProduceIdenticalOffspring_WhenSingleIndividual() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA g1 = newPokemon("G1");
        PokemonGA g2 = newPokemon("G2");
        PokemonGA g3 = newPokemon("G3");

        PokemonTeamGA parent = new PokemonTeamGA(new PokemonGA[]{g1, g2, g3});
        pop.add(parent);
        when(random.nextBoolean()).thenReturn(true, false, true, true, false, false);

        Population<PokemonTeamGA> children = crossover.apply(pop, random);
        assertEquals(2, children.size());
        for (PokemonTeamGA c : children) {
            assertArrayEquals(parent.getCoding(), c.getCoding(), "Self-pair must reproduce identical coding");
        }
    }

    @Test
        // verifies correct number of random calls based on gene count
    void apply_ShouldCallNextBooleanExpectedNumberOfTimes() throws CloneNotSupportedException {
        Population<PokemonTeamGA> pop = new FixedSizePopulation<>(1L, 5);

        PokemonGA[] genesA = {newPokemon("A1"), newPokemon("A2"), newPokemon("A3")};
        PokemonGA[] genesB = {newPokemon("B1"), newPokemon("B2"), newPokemon("B3")};

        PokemonTeamGA p1 = new PokemonTeamGA(genesA);
        PokemonTeamGA p2 = new PokemonTeamGA(genesB);
        pop.add(p1);
        pop.add(p2);

        when(random.nextBoolean()).thenReturn(true, false, true, false, true, false);

        crossover.apply(pop, random);

        // 3 genes * 2 crossovers = 6 total calls
        verify(random, times(6)).nextBoolean();
    }
}

