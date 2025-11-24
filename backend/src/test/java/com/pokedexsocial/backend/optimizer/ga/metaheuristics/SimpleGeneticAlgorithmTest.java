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

import java.util.Collection;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SimpleGeneticAlgorithmTest {

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
                0.5,
                5,
                3
        );
    }

    private Population<TestIndividual> makePopulation(Long id, double... fitnessValues) {
        Population<TestIndividual> pop = new Population<>(id) {};
        for (double f : fitnessValues) {
            pop.add(new TestIndividual(f));
        }
        pop.setBestIndividual(pop.iterator().next());
        return pop;
    }

    @Test
    void run_ShouldEvaluateInitialAndNewGenerations() throws CloneNotSupportedException {
        algorithm.run();
        verify(fitnessFunction, atLeast(2)).evaluate(any(Population.class));
    }

    @Test
    void run_ShouldPerformMutation_WhenProbabilityTriggers() throws CloneNotSupportedException {

        // GA con Random mockato che ritorna sempre 0.1 (< 0.5)
        SimpleGeneticAlgorithm<TestIndividual> deterministicAlg =
                new SimpleGeneticAlgorithm<>(
                        fitnessFunction, initializer, selectionOperator,
                        crossoverOperator, mutationOperator,
                        0.5,  // probabilità mutazione
                        5, 3
                ) {
                    @Override
                    protected Random newRandom() {
                        Random r = mock(Random.class);
                        when(r.nextDouble()).thenReturn(0.1); // forza la mutazione
                        return r;
                    }
                };

        Population<TestIndividual> sel = makePopulation(2L, 1.5, 2.5);
        Population<TestIndividual> cross = makePopulation(3L, 2.0, 2.2);
        Population<TestIndividual> mut = makePopulation(4L, 3.0, 3.1);

        when(selectionOperator.apply(any(), any())).thenReturn(sel);
        when(crossoverOperator.apply(any(), any())).thenReturn(cross);
        when(mutationOperator.apply(any(), any())).thenReturn(mut);

        Results<TestIndividual> result = deterministicAlg.run();

        assertThat(result).isNotNull();

        // Ora È GARANTITO che venga chiamata
        verify(mutationOperator, atLeastOnce()).apply(any(), any());
    }


    @Test
    void run_ShouldSkipMutation_WhenProbabilityDoesNotTrigger() throws CloneNotSupportedException {
        SimpleGeneticAlgorithm<TestIndividual> noMutationAlg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.0,
                5, 3
        );

        Results<TestIndividual> result = noMutationAlg.run();

        assertThat((Collection<? extends TestIndividual>) result.getBestGeneration()).isNotNull();
        verify(mutationOperator, never()).apply(any(), any());
    }

    @Test
    void constructor_ShouldClampNegativeMutationProbability() {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                -0.5,
                5, 3
        );
        assertThat(alg.getMutationProbability()).isEqualTo(1.0);
    }

    @Test
    void run_ShouldReachMaxIterations_WhenAlwaysImproving() throws CloneNotSupportedException {
        Population<TestIndividual> improved = makePopulation(10L, 10.0, 11.0);
        when(selectionOperator.apply(any(), any())).thenReturn(improved);
        when(crossoverOperator.apply(any(), any())).thenReturn(improved);
        when(mutationOperator.apply(any(), any())).thenReturn(improved);

        Results<TestIndividual> result = algorithm.run();

        assertThat(result.getNumberOfIterations()).isEqualTo(5);
    }

    @Test
    void constructor_ShouldClampInvalidMutationProbability() {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                5.0,
                5, 3
        );

        assertThat(getPrivateField(alg, "mutationProbability")).isEqualTo(1.0);
    }

    @Test
    void run_ShouldHandleMinimizationMode() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        Population<TestIndividual> worse = makePopulation(2L, 5.0, 4.0);
        when(selectionOperator.apply(any(), any())).thenReturn(worse);
        when(crossoverOperator.apply(any(), any())).thenReturn(worse);
        when(mutationOperator.apply(any(), any())).thenReturn(worse);

        Results<TestIndividual> result = algorithm.run();

        assertThat((Collection<? extends TestIndividual>) result.getBestGeneration()).isNotNull();
        verify(fitnessFunction, atLeastOnce()).isMaximum();
    }

    @Test
    void run_ShouldStopEarlyExactlyAfterConfiguredNoImprovementIterations() throws CloneNotSupportedException {
        Population<TestIndividual> stagnant = makePopulation(1L, 1.0, 1.0);
        when(selectionOperator.apply(any(), any())).thenReturn(stagnant);
        when(crossoverOperator.apply(any(), any())).thenReturn(stagnant);
        when(mutationOperator.apply(any(), any())).thenReturn(stagnant);

        SimpleGeneticAlgorithm<TestIndividual> early = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,
                50,
                2
        );

        Results<TestIndividual> result = early.run();

        assertThat(result.getNumberOfIterations()).isEqualTo(3);
    }

    @Test
    void run_ShouldNotStopEarly_WhenMaxIterationsNoImprovementsIsZero() throws CloneNotSupportedException {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,
                5,
                0
        );

        Population<TestIndividual> stagn = makePopulation(1L, 1.0, 1.0);
        when(initializer.initialize()).thenReturn(stagn);
        when(selectionOperator.apply(any(), any())).thenReturn(stagn);
        when(crossoverOperator.apply(any(), any())).thenReturn(stagn);
        when(mutationOperator.apply(any(), any())).thenReturn(stagn);

        Results<TestIndividual> result = alg.run();

        assertThat(result.getNumberOfIterations()).isEqualTo(5);
    }

    @Test
    void getters_ShouldReturnClampedConfigurationValues() {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,
                0,
                -10
        );

        assertThat(alg.getMaxIterations()).isEqualTo(1);
        assertThat(alg.getMaxIterationsNoImprovements()).isEqualTo(0);
    }

    @Test
    void constructor_ShouldClampMaxIterations_WhenNegative() {
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5,
                -1,
                5
        );

        assertThat(alg.getMaxIterations()).isEqualTo(1); // NON 0!
    }

    @Test
    void run_ShouldEvaluateNewGenerationsEveryIteration() throws CloneNotSupportedException {
        // Arrange
        Population<TestIndividual> firstGen = makePopulation(1L, 1.0, 1.0);
        Population<TestIndividual> afterSelection = makePopulation(2L, 2.0, 2.0);
        Population<TestIndividual> afterCrossover = makePopulation(3L, 3.0, 3.0);
        Population<TestIndividual> afterMutation = makePopulation(4L, 4.0, 4.0);

        when(initializer.initialize()).thenReturn(firstGen);
        when(selectionOperator.apply(any(), any())).thenReturn(afterSelection);
        when(crossoverOperator.apply(any(), any())).thenReturn(afterCrossover);
        when(mutationOperator.apply(any(), any())).thenReturn(afterMutation);

        // Act
        algorithm.run();

        // Assert —  evaluate MUST be invoked:
        // 1) on firstGeneration
        // 2) on each iteration's newGeneration
        verify(fitnessFunction, atLeast(2)).evaluate(any(Population.class));
    }

    @Test
    void run_ShouldTriggerMutation_WhenRandomEqualsMutationProbability() throws CloneNotSupportedException {
        // Mutation probability = 0.5
        SimpleGeneticAlgorithm<TestIndividual> alg = new SimpleGeneticAlgorithm<>(
                fitnessFunction, initializer, selectionOperator,
                crossoverOperator, mutationOperator,
                0.5, // 👈 valore chiave
                5, 3
        ) {
            @Override
            protected Random newRandom() {
                Random r = mock(Random.class);
                when(r.nextDouble()).thenReturn(0.5);
                return r;
            }
        };

        Population<TestIndividual> afterMutation = makePopulation(100L, 10.0);
        when(mutationOperator.apply(any(), any())).thenReturn(afterMutation);

        Results<TestIndividual> result = alg.run();

        assertThat(result).isNotNull();

        // La mutazione DEVE essere applicata
        verify(mutationOperator, atLeastOnce()).apply(any(), any());
    }

    @Test
    void run_ShouldNotTreatEqualFitnessAsImprovement_InMinimizationMode() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        // first generation
        Population<TestIndividual> first = makePopulation(1L, 5.0);
        when(initializer.initialize()).thenReturn(first);

        // next generation with SAME fitness (compareTo == 0)
        Population<TestIndividual> equal = spy(makePopulation(2L, 5.0));
        when(selectionOperator.apply(any(), any())).thenReturn(equal);
        when(crossoverOperator.apply(any(), any())).thenReturn(equal);
        when(mutationOperator.apply(any(), any())).thenReturn(equal);

        Results<TestIndividual> res = algorithm.run();

        // equal fitness MUST NOT count as improvement
        verify(equal, never()).setBestIndividual(any());
    }

    @Test
    void run_ShouldDetectImprovementOnlyWhenFitnessDecreases_InMinimizationMode() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        Population<TestIndividual> first = makePopulation(1L, 10.0);
        Population<TestIndividual> better = makePopulation(2L, 5.0); // LOWER is better

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(better);
        when(crossoverOperator.apply(any(), any())).thenReturn(better);
        when(mutationOperator.apply(any(), any())).thenReturn(better);

        Results<TestIndividual> res = algorithm.run();

        // bestGeneration MUST update to "better"
        assertThat(res.getBestGeneration().iterator().next().getFitness()).isEqualTo(5.0);
    }

    @Test
    void run_ShouldRecognizeCompareLessThanZeroAsImprovement_InMinimizationMode()
            throws CloneNotSupportedException {

        when(fitnessFunction.isMaximum()).thenReturn(false);

        // first generation
        Population<TestIndividual> first = makePopulation(1L, 10.0);

        // second generation LOWER fitness (compareTo < 0)
        Population<TestIndividual> better = makePopulation(2L, 1.0);

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(better);
        when(crossoverOperator.apply(any(), any())).thenReturn(better);
        when(mutationOperator.apply(any(), any())).thenReturn(better);

        Results<TestIndividual> res = algorithm.run();

        // verify improvement is detected
        assertThat(res.getBestIndividual().getFitness()).isEqualTo(1.0);
    }

    @Test
    void getMaxIterationsNoImprovements_ShouldReturnConfiguredValue() {
        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(
                        fitnessFunction, initializer, selectionOperator, crossoverOperator, mutationOperator,
                        0.5,
                        10,   // maxIterations
                        7     // maxIterationsNoImprovements
                );

        assertThat(alg.getMaxIterationsNoImprovements()).isEqualTo(7);
    }

    @Test
    void run_Maximization_ShouldNotCountEqualFitnessAsImprovement() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(true);

        Population<TestIndividual> first = makePopulation(1L, 5.0);
        Population<TestIndividual> equal = makePopulation(2L, 5.0);

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(equal);
        when(crossoverOperator.apply(any(), any())).thenReturn(equal);
        when(mutationOperator.apply(any(), any())).thenReturn(equal);

        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(fitnessFunction, initializer,
                        selectionOperator, crossoverOperator, mutationOperator,
                        0.0, 5, 2); // early stop at 2

        Results<TestIndividual> res = alg.run();

        assertThat(res.getNumberOfIterations()).isEqualTo(3);
    }

    @Test
    void run_Maximization_ShouldDetectImprovementCorrectly() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(true);

        Population<TestIndividual> first = makePopulation(1L, 5.0);
        Population<TestIndividual> better = makePopulation(2L, 6.0);

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(better);
        when(crossoverOperator.apply(any(), any())).thenReturn(better);
        when(mutationOperator.apply(any(), any())).thenReturn(better);

        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(fitnessFunction, initializer,
                        selectionOperator, crossoverOperator, mutationOperator,
                        0.0, 5, 5);

        Results<TestIndividual> res = alg.run();

        assertThat(res.getBestIndividual().getFitness()).isEqualTo(6.0);
    }


    @Test
    void run_Minimization_ShouldNotCountEqualFitnessAsImprovement() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        Population<TestIndividual> first = makePopulation(1L, 5.0);
        Population<TestIndividual> equal = makePopulation(2L, 5.0);

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(equal);
        when(crossoverOperator.apply(any(), any())).thenReturn(equal);
        when(mutationOperator.apply(any(), any())).thenReturn(equal);

        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(fitnessFunction, initializer,
                        selectionOperator, crossoverOperator, mutationOperator,
                        0.0, 5, 2);

        Results<TestIndividual> res = alg.run();

        assertThat(res.getNumberOfIterations()).isEqualTo(3);
    }

    @Test
    void run_Minimization_ShouldDetectImprovementCorrectly() throws CloneNotSupportedException {
        when(fitnessFunction.isMaximum()).thenReturn(false);

        Population<TestIndividual> first = makePopulation(1L, 5.0);
        Population<TestIndividual> better = makePopulation(2L, 4.0);

        when(initializer.initialize()).thenReturn(first);
        when(selectionOperator.apply(any(), any())).thenReturn(better);
        when(crossoverOperator.apply(any(), any())).thenReturn(better);
        when(mutationOperator.apply(any(), any())).thenReturn(better);

        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(fitnessFunction, initializer,
                        selectionOperator, crossoverOperator, mutationOperator,
                        0.0, 5, 5);

        Results<TestIndividual> res = alg.run();

        assertThat(res.getBestIndividual().getFitness()).isEqualTo(4.0);
    }

    @Test
    void constructor_ShouldAcceptBoundaryZeroMutationProbability() {
        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(
                        fitnessFunction, initializer, selectionOperator, crossoverOperator, mutationOperator,
                        0.0, 10, 5
                );

        assertThat(alg.getMutationProbability()).isEqualTo(0.0);
    }

    @Test
    void constructor_ShouldAcceptBoundaryOneMutationProbability() {
        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(
                        fitnessFunction, initializer, selectionOperator, crossoverOperator, mutationOperator,
                        1.0, 10, 5
                );

        assertThat(alg.getMutationProbability()).isEqualTo(1.0);
    }

    @Test
    void run_ShouldEvaluateInitialPopulation() throws CloneNotSupportedException {
        Population<TestIndividual> initial = makePopulation(1L, 1.0, 2.0);
        when(initializer.initialize()).thenReturn(initial);

        SimpleGeneticAlgorithm<TestIndividual> alg =
                new SimpleGeneticAlgorithm<>(
                        fitnessFunction, initializer, selectionOperator,
                        crossoverOperator, mutationOperator,
                        0.5, 3, 1
                );

        alg.run();

        // Must be called AT LEAST ONCE on the INITIAL population
        verify(fitnessFunction, atLeastOnce()).evaluate(initial);
    }

    private Object getPrivateField(Object obj, String fieldName) {
        try {
            var f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}



