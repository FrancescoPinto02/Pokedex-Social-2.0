package com.pokedexsocial.backend.optimizer.ga.operators.mutation;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonSwapMutationTest {

    @Mock
    private PokemonGenerator pokemonGenerator;

    @Mock
    private Random random;

    private PokemonSwapMutation mutationOperator;

    @BeforeEach
    void setUp() {
        mutationOperator = new PokemonSwapMutation(pokemonGenerator, 0.3);
    }

    private PokemonGA newPokemon(String name) {
        return mock(PokemonGA.class, name);
    }

    @Test
        // Tests id increment and that original population is unchanged
    void apply_ShouldIncrementIdAndKeepOriginalUnchanged() throws CloneNotSupportedException {
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(10L, 5);
        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonTeamGA ind = new PokemonTeamGA(new PokemonGA[]{p1, p2});
        population.add(ind);

        when(random.nextDouble()).thenReturn(1.0); // > 0.3, no mutation

        Population<PokemonTeamGA> result = mutationOperator.apply(population, random);

        assertNotSame(population, result);
        assertEquals(10L, population.getId());
        assertEquals(11L, result.getId());
        assertEquals(1, population.size());
        assertEquals(1, result.size());
        assertSame(ind, result.iterator().next(), "Should copy the original when no mutation occurs");
    }

    @Test
        // Tests that an individual mutates when random <= mutationProbability
    void apply_ShouldMutateIndividual_WhenProbabilitySatisfied() throws CloneNotSupportedException {
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 5);
        PokemonGA a1 = newPokemon("A1");
        PokemonGA a2 = newPokemon("A2");
        PokemonTeamGA ind = new PokemonTeamGA(new PokemonGA[]{a1, a2});
        population.add(ind);

        PokemonGA generated = newPokemon("NewPoke");

        when(random.nextDouble()).thenReturn(0.1); // <= 0.3 triggers mutation
        when(random.nextInt(2)).thenReturn(1); // mutate position 1
        when(pokemonGenerator.generatePokemon()).thenReturn(generated);

        Population<PokemonTeamGA> result = mutationOperator.apply(population, random);
        PokemonTeamGA mutated = result.iterator().next();

        assertNotSame(ind, mutated, "Mutated individual should be a new instance");
        assertArrayEquals(new PokemonGA[]{a1, generated}, mutated.getCoding(), "Second gene should be replaced");
        verify(pokemonGenerator, times(1)).generatePokemon();
    }

    @Test
        // Tests that no mutation occurs when random > mutationProbability
    void apply_ShouldNotMutateIndividual_WhenProbabilityNotSatisfied() throws CloneNotSupportedException {
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 5);
        PokemonGA g1 = newPokemon("G1");
        PokemonGA g2 = newPokemon("G2");
        PokemonTeamGA ind = new PokemonTeamGA(new PokemonGA[]{g1, g2});
        population.add(ind);

        when(random.nextDouble()).thenReturn(0.9); // > 0.3 → no mutation

        Population<PokemonTeamGA> result = mutationOperator.apply(population, random);
        PokemonTeamGA child = result.iterator().next();

        assertSame(ind, child, "Individual should be unchanged when mutation not triggered");
        verify(pokemonGenerator, never()).generatePokemon();
    }

    @Test
        // Tests that all individuals mutate when probability = 1
    void apply_ShouldMutateAll_WhenProbabilityIsOne() throws CloneNotSupportedException {
        mutationOperator = new PokemonSwapMutation(pokemonGenerator, 1.0);
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 5);

        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonGA q1 = newPokemon("Q1");
        PokemonGA q2 = newPokemon("Q2");

        PokemonTeamGA ind1 = new PokemonTeamGA(new PokemonGA[]{p1, p2});
        PokemonTeamGA ind2 = new PokemonTeamGA(new PokemonGA[]{q1, q2});
        population.add(ind1);
        population.add(ind2);

        when(random.nextDouble()).thenReturn(0.2, 0.2);
        when(random.nextInt(2)).thenReturn(0, 1);
        when(pokemonGenerator.generatePokemon()).thenReturn(newPokemon("N1"), newPokemon("N2"));

        Population<PokemonTeamGA> result = mutationOperator.apply(population, random);

        assertEquals(2, result.size());
        for (PokemonTeamGA child : result) {
            assertTrue(Arrays.stream(child.getCoding())
                    .anyMatch(p -> mockingDetails(p)
                            .getMockCreationSettings()
                            .getMockName()
                            .toString()
                            .contains("N")));
        }
        verify(pokemonGenerator, times(2)).generatePokemon();
    }

    @Test
        // Tests that no individuals mutate when probability = 0
    void apply_ShouldNotMutateAny_WhenProbabilityIsZero() throws CloneNotSupportedException {
        mutationOperator = new PokemonSwapMutation(pokemonGenerator, 0.0);
        Population<PokemonTeamGA> population = new FixedSizePopulation<>(1L, 5);

        PokemonGA p1 = newPokemon("P1");
        PokemonGA p2 = newPokemon("P2");
        PokemonTeamGA ind = new PokemonTeamGA(new PokemonGA[]{p1, p2});
        population.add(ind);

        when(random.nextDouble()).thenReturn(0.5);

        Population<PokemonTeamGA> result = mutationOperator.apply(population, random);
        assertSame(ind, result.iterator().next());
        verify(pokemonGenerator, never()).generatePokemon();
    }

    @Test
        // Tests direct mutate() logic: exactly one gene is replaced
    void mutate_ShouldReplaceExactlyOneGene_WhenCalledDirectly() throws Exception {
        PokemonGA g1 = newPokemon("G1");
        PokemonGA g2 = newPokemon("G2");
        PokemonGA g3 = newPokemon("G3");
        PokemonGA newGene = newPokemon("NEW");

        PokemonTeamGA parent = new PokemonTeamGA(new PokemonGA[]{g1, g2, g3});

        when(random.nextInt(3)).thenReturn(1);
        when(pokemonGenerator.generatePokemon()).thenReturn(newGene);

        // Invoke private method reflectively (white-box)
        var method = PokemonSwapMutation.class.getDeclaredMethod("mutate", PokemonTeamGA.class, Random.class);
        method.setAccessible(true);
        PokemonTeamGA mutated = (PokemonTeamGA) method.invoke(mutationOperator, parent, random);

        assertArrayEquals(new PokemonGA[]{g1, newGene, g3}, mutated.getCoding());
        assertNotSame(parent.getCoding(), mutated.getCoding());
        verify(pokemonGenerator, times(1)).generatePokemon();
    }
}

