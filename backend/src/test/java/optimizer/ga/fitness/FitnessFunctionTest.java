package optimizer.ga.fitness;

import com.pokedexsocial.backend.optimizer.ga.fitness.FitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.Individual;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for FitnessFunction covering max/min selection, iteration/evaluation, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class FitnessFunctionTest {

    // Concrete minimal Individual for testing
    private static class TestIndividual extends Individual {
        TestIndividual(double fitness) { super(fitness); }
    }

    // Concrete minimal Population for testing
    private static class TestPopulation extends Population<TestIndividual> {
        TestPopulation(long id) { super(id); }
    }

    // Concrete FitnessFunction that "evaluates" by doubling the individual's fitness (deterministic & monotone)
    private static class MaxFitnessFunction extends FitnessFunction<TestIndividual> {
        MaxFitnessFunction() { super(true); }
        @Override public void evaluate(TestIndividual individual) {
            individual.setFitness(individual.getFitness() * 2);
        }
    }

    private static class MinFitnessFunction extends FitnessFunction<TestIndividual> {
        MinFitnessFunction() { super(false); }
        @Override public void evaluate(TestIndividual individual) {
            individual.setFitness(individual.getFitness() * 2);
        }
    }

    // Use spies to verify per-individual evaluation invocation counts
    @Spy
    @InjectMocks
    private MaxFitnessFunction maxFunction = new MaxFitnessFunction();

    @Spy
    @InjectMocks
    private MinFitnessFunction minFunction = new MinFitnessFunction();

    // ---------------------- Tests ----------------------

    /** Ensures that, when isMaximum=true, the individual with highest post-evaluation fitness is chosen as best. */
    @Test
    void evaluate_ShouldSelectMaxIndividual_WhenIsMaximumTrue() {
        TestPopulation population = new TestPopulation(1L);
        TestIndividual low = new TestIndividual(1.0);
        TestIndividual mid = new TestIndividual(3.0);
        TestIndividual high = new TestIndividual(5.0);
        population.add(low);
        population.add(mid);
        population.add(high);

        maxFunction.evaluate(population);

        assertThat(population.getBestIndividual()).isSameAs(high); // doubling preserves ordering
        verify(maxFunction, times(1)).evaluate(low);
        verify(maxFunction, times(1)).evaluate(mid);
        verify(maxFunction, times(1)).evaluate(high);
    }

    /** Ensures that, when isMaximum=false, the individual with lowest post-evaluation fitness is chosen as best. */
    @Test
    void evaluate_ShouldSelectMinIndividual_WhenIsMaximumFalse() {
        TestPopulation population = new TestPopulation(2L);
        TestIndividual low = new TestIndividual(1.0);
        TestIndividual mid = new TestIndividual(3.0);
        TestIndividual high = new TestIndividual(5.0);
        population.add(low);
        population.add(mid);
        population.add(high);

        minFunction.evaluate(population);

        assertThat(population.getBestIndividual()).isSameAs(low); // doubling preserves ordering
        verify(minFunction, times(1)).evaluate(low);
        verify(minFunction, times(1)).evaluate(mid);
        verify(minFunction, times(1)).evaluate(high);
    }

    /** Verifies that evaluate is called exactly once for a single-element population and that element becomes best (max). */
    @Test
    void evaluate_ShouldEvaluateOnceAndPickOnlyElement_WhenSingleElementAndMaximum() {
        TestPopulation population = new TestPopulation(3L);
        TestIndividual only = new TestIndividual(2.0);
        population.add(only);

        maxFunction.evaluate(population);

        assertThat(population.getBestIndividual()).isSameAs(only);
        verify(maxFunction, times(1)).evaluate(only);
    }

    /** Verifies that evaluate is called exactly once for a single-element population and that element becomes best (min). */
    @Test
    void evaluate_ShouldEvaluateOnceAndPickOnlyElement_WhenSingleElementAndMinimum() {
        TestPopulation population = new TestPopulation(4L);
        TestIndividual only = new TestIndividual(2.0);
        population.add(only);

        minFunction.evaluate(population);

        assertThat(population.getBestIndividual()).isSameAs(only);
        verify(minFunction, times(1)).evaluate(only);
    }

    /** Ensures NoSuchElementException is thrown on empty population when selecting maximum. */
    @Test
    void evaluate_ShouldThrowException_WhenPopulationEmptyAndMaximum() {
        TestPopulation population = new TestPopulation(5L);

        assertThatThrownBy(() -> maxFunction.evaluate(population))
                .isInstanceOf(NoSuchElementException.class);

        // no individuals -> no per-individual evaluation
        verify(maxFunction, times(0)).evaluate((TestIndividual) org.mockito.Mockito.any());
        assertThat(population.getBestIndividual()).isNull();
    }

    /** Ensures NoSuchElementException is thrown on empty population when selecting minimum. */
    @Test
    void evaluate_ShouldThrowException_WhenPopulationEmptyAndMinimum() {
        TestPopulation population = new TestPopulation(6L);

        assertThatThrownBy(() -> minFunction.evaluate(population))
                .isInstanceOf(NoSuchElementException.class);

        verify(minFunction, times(0)).evaluate((TestIndividual) org.mockito.Mockito.any());
        assertThat(population.getBestIndividual()).isNull();
    }

    /** Verifies the isMaximum() flag is reported correctly for both concrete functions. */
    @Test
    void isMaximum_ShouldReturnConfiguredFlag_WhenQueried() {
        assertThat(maxFunction.isMaximum()).isTrue();
        assertThat(minFunction.isMaximum()).isFalse();
    }
}

