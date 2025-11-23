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
        // Verifies roulette selection works proportionally to fitness
    void apply_ShouldSelectBasedOnFitnessProportion_WhenFitnessesAreDifferent() throws CloneNotSupportedException {
        Population<Individual> population = new FixedSizePopulation<>(5L, 3);
        TestIndividual a = new TestIndividual("A", 1.0);
        TestIndividual b = new TestIndividual("B", 2.0);
        TestIndividual c = new TestIndividual("C", 3.0);
        population.add(a);
        population.add(b);
        population.add(c);

        when(random.nextDouble()).thenReturn(0.1, 0.3, 0.8);

        Population<Individual> result = selectionOperator.apply(population, random);

        assertEquals(3, result.size());
        assertEquals(6L, result.getId()); // ID increments
        Set<String> names = result.stream().map(Object::toString).collect(Collectors.toSet());
        assertTrue(names.containsAll(Set.of("A", "B", "C")));
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
    // Verifica che i confini sinistro e destro dell'intervallo siano gestiti correttamente
    void apply_ShouldSelectCorrectIntervals_OnLeftAndRightBoundaries() throws CloneNotSupportedException {
        // popolazione: A = 1.0, B = 3.0 → total = 4.0
        // intervalli:
        //  A: [0.0, 0.25)
        //  B: [0.25, 1.0)
        Population<Individual> population = new FixedSizePopulation<>(2L, 2);
        TestIndividual a = new TestIndividual("A", 1.0);
        TestIndividual b = new TestIndividual("B", 3.0);
        population.add(a);
        population.add(b);

        double rightBoundaryOfA = 1.0 / 4.0; // 0.25

        // primo giro: pointer = 0.0  → deve selezionare A
        // secondo giro: pointer = 0.25 → deve selezionare B (non più A!)
        when(random.nextDouble()).thenReturn(0.0, rightBoundaryOfA);

        Population<Individual> result = selectionOperator.apply(population, random);
        List<Individual> resultList = new ArrayList<>(result);

        assertEquals(2, resultList.size(), "La nuova popolazione deve avere 2 individui");

        assertEquals("A", resultList.get(0).toString(),
                "Pointer 0.0 deve cadere nel primo intervallo (A)");
        assertEquals("B", resultList.get(1).toString(),
                "Pointer sul confine destro di A deve selezionare il secondo intervallo (B)");
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
    void apply_ShouldSelectElement_WhenPointerMatchesExactStartPosition() throws CloneNotSupportedException {

        Population<Individual> population = new FixedSizePopulation<>(5L, 3);
        TestIndividual a = new TestIndividual("A", 1.0);  // [0.0, 1/6)
        TestIndividual b = new TestIndividual("B", 2.0);  // [1/6, 3/6)
        TestIndividual c = new TestIndividual("C", 3.0);  // [3/6, 1.0)
        population.add(a);
        population.add(b);
        population.add(c);

        double startOfB = 1.0 / 6.0;

        when(random.nextDouble()).thenReturn(startOfB);

        Population<Individual> result = selectionOperator.apply(population, random);
        String selected = result.iterator().next().toString();

        assertEquals("B", selected,
                "Pointer equal to interval start must select that interval’s individual");
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

}


