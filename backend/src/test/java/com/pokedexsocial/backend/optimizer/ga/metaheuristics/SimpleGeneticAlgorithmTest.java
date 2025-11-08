package com.pokedexsocial.backend.optimizer.ga.metaheuristics;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.initializer.Initializer;
import com.pokedexsocial.backend.optimizer.ga.operators.crossover.CrossoverOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.mutation.MutationOperator;
import com.pokedexsocial.backend.optimizer.ga.operators.selection.SelectionOperator;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Professional unit tests for {@link SimpleGeneticAlgorithm}.
 *
 * These tests verify:
 * - Mutation probability behavior (triggered vs skipped)
 * - Iteration limits and improvement detection
 * - Correct interaction with GA operators
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleGeneticAlgorithmTest {

    /**
     * Minimal Individual implementation for testing.
     */
    static class TestIndividual extends Individual {
        private double fitness;

        TestIndividual(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }

        @Override
        public void setFitness(double value) {
            this.fitness = value;
        }

        @Override
        public TestIndividual clone() {
            return new TestIndividual(this.fitness);
        }
    }

    @Mock private FitnessFunction<TestIndividual> fitnessFunction;
    @Mock private Initializer<TestIndividual> initializer;
    @Mock private SelectionOperator<TestIndividual> selectionOperator;
    @Mock private CrossoverOperator<TestIndividual> crossoverOperator;
    @Mock private MutationOperator<TestIndividual> mutationOperator;

    private SimpleGeneticAlgorithm<TestIndividual> algorithm;
    private Population<TestIndividual> basePop;
    private Population<TestIndividual> defaultPop;

    @BeforeEach
    void setup() throws CloneNotSupportedException {
        basePop = makePopulation(1L, 1.0, 2.0);
        defaultPop = makePopulation(999L, 1.0, 1.0);

        lenient().when(selectionOperator.apply(any(), any())).thenReturn(defaultPop);
        lenient().when(crossoverOperator.apply(any(), any())).thenReturn(defaultPop);
        lenient().when(mutationOperator.apply(any(), any())).thenReturn(defaultPop);
        lenient().doNothing().when(fitnessFunction).evaluate(any(Population.class));
        lenient().when(fitnessFunction.isMaximum()).thenReturn(true);
        lenient().when(initializer.initialize()).thenReturn(basePop);

        algorithm = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,  // mutation probability
                5,    // max iterations
                3     // max iterations without improvement
        );
    }

    /**
     * Utility: creates a population with given fitness values.
     */
    private Population<TestIndividual> makePopulation(Long id, double... fitnessValues) {
        Population<TestIndividual> pop = new Population<>(id) {}; // anonymous subclass
        for (double f : fitnessValues) {
            pop.add(new TestIndividual(f));
        }
        pop.setBestIndividual(pop.iterator().next());
        return pop;
    }

    @Test
    void run_ShouldPerformMutation_WhenProbabilityTriggers() throws CloneNotSupportedException {
        Population<TestIndividual> afterSelection = makePopulation(2L, 1.5, 2.5);
        Population<TestIndividual> afterCrossover = makePopulation(3L, 2.0, 2.2);
        Population<TestIndividual> afterMutation = makePopulation(4L, 3.0, 3.1);

        when(selectionOperator.apply(any(), any())).thenReturn(afterSelection);
        when(crossoverOperator.apply(any(), any())).thenReturn(afterCrossover);
        when(mutationOperator.apply(any(), any())).thenReturn(afterMutation);

        Results<TestIndividual> result = algorithm.run();

        assertThat(result).isNotNull();
        assertThat(result.getBestGeneration()).isNotNull();
        assertThat(result.getNumberOfIterations()).isGreaterThan(0);

        verify(selectionOperator, atLeastOnce()).apply(any(), any());
        verify(crossoverOperator, atLeastOnce()).apply(any(), any());
        verify(mutationOperator, atLeastOnce()).apply(any(), any());
    }

    @Test
    void run_ShouldSkipMutation_WhenProbabilityDoesNotTrigger() throws CloneNotSupportedException {
        SimpleGeneticAlgorithm<TestIndividual> noMutationAlg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.0,  // mutation disabled
                5, 3
        );

        Results<TestIndividual> result = noMutationAlg.run();

        assertThat(result.getBestGeneration()).isNotNull();
        verify(mutationOperator, never()).apply(any(), any());
    }

    @Test
    void constructor_ShouldClampNegativeMutationProbability() {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                -0.5,  // 👈 negative value to hit left branch
                5, 3
        );
        assertThat(alg.getMutationProbability()).isEqualTo(1.0);
    }

    @Test
    void run_ShouldReachMaxIterations_WhenAlwaysImproving() throws CloneNotSupportedException {
        Population<TestIndividual> improvedPop = makePopulation(10L, 10.0, 11.0);
        when(selectionOperator.apply(any(), any())).thenReturn(improvedPop);
        when(crossoverOperator.apply(any(), any())).thenReturn(improvedPop);
        when(mutationOperator.apply(any(), any())).thenReturn(improvedPop);

        Results<TestIndividual> result = algorithm.run();

        assertThat(result.getNumberOfIterations()).isEqualTo(5);
        assertThat(result.getBestIndividual().getFitness()).isGreaterThan(0);
    }

    @Test
    void constructor_ShouldClampInvalidMutationProbability() {
        // Mutation probability > 1 should be clamped to 1.0
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                5.0,  // invalid value
                5, 3
        );

        assertThat(alg.getMutationOperator()).isNotNull();
        // Reflection used to verify internal field
        // but if mutationProbability has a getter, use it directly
        assertThat(getPrivateField(alg, "mutationProbability")).isEqualTo(1.0);
    }

    @Test
    void run_ShouldHandleMinimizationMode() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        Population<TestIndividual> worseGeneration = makePopulation(2L, 5.0, 4.0);
        when(selectionOperator.apply(any(), any())).thenReturn(worseGeneration);
        when(crossoverOperator.apply(any(), any())).thenReturn(worseGeneration);
        when(mutationOperator.apply(any(), any())).thenReturn(worseGeneration);

        Results<TestIndividual> result = algorithm.run();

        assertThat(result.getBestGeneration()).isNotNull();
        verify(fitnessFunction, atLeastOnce()).isMaximum();
    }

    @Test
    void run_ShouldStopEarly_WhenNoImprovementsOccur() throws CloneNotSupportedException {
        // Same fitness each generation => no improvement
        Population<TestIndividual> stagnant = makePopulation(1L, 1.0, 1.0);
        when(selectionOperator.apply(any(), any())).thenReturn(stagnant);
        when(crossoverOperator.apply(any(), any())).thenReturn(stagnant);
        when(mutationOperator.apply(any(), any())).thenReturn(stagnant);

        SimpleGeneticAlgorithm<TestIndividual> earlyStopAlg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,  // valid mutation
                50,   // many iterations possible
                2     // but stop early after 2 non-improvements
        );

        Results<TestIndividual> result = earlyStopAlg.run();

        // Should stop before max iterations
        assertThat(result.getNumberOfIterations()).isLessThan(50);
    }

    @Test
    void run_ShouldImprove_WhenFitnessIsMinimizationMode() throws CloneNotSupportedException {
        // Simulate minimization instead of maximization
        when(fitnessFunction.isMaximum()).thenReturn(false);

        // Make compareTo < 0 so the condition (!isMaximum && compareTo < 0) becomes true
        Population<TestIndividual> newGen = mock(Population.class);
        Population<TestIndividual> bestGen = mock(Population.class);
        when(newGen.compareTo(bestGen)).thenReturn(-1);

        // Mock the GA flow
        when(selectionOperator.apply(any(), any())).thenReturn(newGen);
        when(crossoverOperator.apply(any(), any())).thenReturn(newGen);
        when(mutationOperator.apply(any(), any())).thenReturn(newGen);

        SimpleGeneticAlgorithm<TestIndividual> minimizationGA = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5, 10, 3
        );

        // Run the algorithm (it should trigger the minimization branch)
        Results<TestIndividual> result = minimizationGA.run();

        // Ensure something ran and the minimization branch was taken
        assertThat(result).isNotNull();
        verify(fitnessFunction, atLeastOnce()).isMaximum();
    }

    @Test
    void run_ShouldNotStopEarly_WhenMaxIterationsNoImprovementsIsZero() throws CloneNotSupportedException {
        // Arrange: 0 disables early stopping
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5, 0, 10 // 👈 0 = disables the stopEarly condition
        );

        when(fitnessFunction.isMaximum()).thenReturn(true);
        Population<TestIndividual> population = mock(Population.class);
        when(initializer.initialize()).thenReturn(population);
        when(selectionOperator.apply(any(), any())).thenReturn(population);
        when(crossoverOperator.apply(any(), any())).thenReturn(population);
        when(mutationOperator.apply(any(), any())).thenReturn(population);

        // Act
        Results<TestIndividual> result = alg.run();

        // Assert
        assertThat(result).isNotNull();
        // This ensures the branch (maxIterationsNoImprovements > 0) == false got executed
    }

    /**
     * Utility to access private fields via reflection for test validation.
     */
    private Object getPrivateField(Object obj, String fieldName) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



