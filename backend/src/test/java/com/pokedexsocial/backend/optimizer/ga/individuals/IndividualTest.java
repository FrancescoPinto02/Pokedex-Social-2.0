package com.pokedexsocial.backend.optimizer.ga.individuals;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndividualTest {

    /**
     * Semplice implementazione concreta per testare Individual.
     */
    static class TestIndividual extends Individual {
        TestIndividual(double f) {
            super(f);
        }
        @Override
        public TestIndividual clone() throws CloneNotSupportedException {
            return (TestIndividual) super.clone();
        }
    }

    // ---------------------------------------------------------
    // 1) Uccide mutanti su getFitness()
    // ---------------------------------------------------------
    @Test
    void getFitness_ShouldReturnCorrectValue() {
        Individual ind = new TestIndividual(7.5);
        assertThat(ind.getFitness()).isEqualTo(7.5);
    }

    // ---------------------------------------------------------
    // 2) Uccide mutanti su compareTo():
    //      - removed conditional
    //      - changed conditional boundary
    //      - replaced int return with 0
    // ---------------------------------------------------------

    @Test
    void compareTo_ShouldReturnNegative_WhenThisIsLessFit() {
        Individual low = new TestIndividual(1.0);
        Individual high = new TestIndividual(5.0);

        int result = low.compareTo(high);

        assertThat(result).isNegative(); // MUST be < 0
    }

    @Test
    void compareTo_ShouldReturnPositive_WhenThisIsMoreFit() {
        Individual high = new TestIndividual(10.0);
        Individual low = new TestIndividual(3.0);

        int result = high.compareTo(low);

        assertThat(result).isPositive(); // MUST be > 0
    }

    @Test
    void compareTo_ShouldReturnZero_WhenFitnessEquals() {
        Individual a = new TestIndividual(4.0);
        Individual b = new TestIndividual(4.0);

        int result = a.compareTo(b);

        assertThat(result).isZero(); // MUST be == 0
    }

    /**
     * Test specifico per boundary mutation:
     * se il mutante cambia < in <= oppure > in >=
     * questo test FALLISCE.
     */
    @Test
    void compareTo_ShouldDifferentiateVeryCloseFitness() {
        Individual a = new TestIndividual(5.0);
        Individual b = new TestIndividual(5.0000001);

        // a < b, quindi deve essere NEGATIVO
        assertThat(a.compareTo(b)).isNegative();

        // b > a, quindi deve essere POSITIVO
        assertThat(b.compareTo(a)).isPositive();
    }

    // ---------------------------------------------------------
    // 3) Uccide mutante su clone()
    // ---------------------------------------------------------
    @Test
    void clone_ShouldProduceDifferentInstanceWithSameValue() throws Exception {
        TestIndividual original = new TestIndividual(9.0);
        TestIndividual cloned = original.clone();

        assertThat(cloned).isNotSameAs(original);
        assertThat(cloned.getFitness()).isEqualTo(original.getFitness());
    }
}
