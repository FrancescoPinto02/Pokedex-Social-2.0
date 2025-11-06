package com.pokedexsocial.backend.optimizer.ga.fitness;

import com.pokedexsocial.backend.optimizer.ga.fitness.PokemonTeamFitnessFunction;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.offset;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PokemonTeamFitnessFunction.
 * Pure unit tests (no Spring context), JUnit5 + Mockito + AssertJ.
 */
@ExtendWith(MockitoExtension.class)
class PokemonTeamFitnessFunctionTest {

    @InjectMocks
    private PokemonTeamFitnessFunction fitnessFunction;

    @Mock
    private PokemonTeamGA team;

    @Mock
    private PokemonGA p1;

    @Mock
    private PokemonGA p2;

    @Mock
    private PokemonType type1a;
    @Mock
    private PokemonType type2a;
    @Mock
    private PokemonType type1b;
    @Mock
    private PokemonType type2b;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure weights are initialized as in @Value defaults without a Spring context
        setPrivateDouble(fitnessFunction, "LOW_WEIGHT", 0.5d);
        setPrivateDouble(fitnessFunction, "NORMAL_WEIGHT", 1.0d);
        setPrivateDouble(fitnessFunction, "HIGH_WEIGHT", 1.5d);
    }

    // --- helpers ---

    private static void setPrivateDouble(Object target, String field, double value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.setDouble(target, value);
    }

    /** Mirror of production formula with clamping used for assertions (kept local to the test). */
    private static double normalize(double x, double minX, double maxX, double minY, double maxY) {
        double normalized = (x - minX) / (maxX - minX) * (maxY - minY) + minY;
        if (Double.isNaN(normalized)) return normalized; // preserve NaN if happens in edge cases
        return Math.max(0.0, Math.min(100.0, normalized));
    }

    // =============================================================================================
    // Tests
    // =============================================================================================

    @Test
    @DisplayName("evaluate_ShouldSetZeroFitness_WhenMoreThanOneMegaEvolution")
        // Verifies the early-return branch: teams with >1 mega evolution are invalid → fitness = 0
    void evaluate_ShouldSetZeroFitness_WhenMoreThanOneMegaEvolution() {
        when(p1.isMegaEvolution()).thenReturn(true);
        when(p2.isMegaEvolution()).thenReturn(true);
        when(team.getCoding()).thenReturn(new PokemonGA[]{p1, p2});

        fitnessFunction.evaluate(team);

        verify(team).setFitness(0.0);
        verifyNoMoreInteractions(team);
    }

    @Test
    @DisplayName("evaluate_ShouldComputeExpectedFitness_WhenValidMixedTeam")
        // Happy-path: exactly 0 or 1 mega, mixed rarities, mixed types, non-empty resistances/weaknesses
        // Asserts the composed formula HIGH*avg + NORMAL*(types + res + legendary) + HIGH*commonWeaknesses
    void evaluate_ShouldComputeExpectedFitness_WhenValidMixedTeam() throws Exception {
        // given (one Mega allowed → compute path)
        when(p1.isMegaEvolution()).thenReturn(false);
        when(p2.isMegaEvolution()).thenReturn(false);

        // totals: one above standard (legendary-like) and one normal
        when(p1.getTotal()).thenReturn(650);    // > MAX_TOTAL_STATS_STANDARD → capped at 600 inside average
        when(p2.getTotal()).thenReturn(400);

        // rarities → legendary count score
        when(p1.getRarity()).thenReturn(PokemonRarity.LEGENDARY); // +2
        when(p2.getRarity()).thenReturn(PokemonRarity.COMMON);    // +0

        // typing: p1 monotype, p2 dual-type
        when(p1.getType1()).thenReturn(type1a);
        when(p1.getType2()).thenReturn(type2a);
        when(type1a.getName()).thenReturn(PokemonTypeName.FIRE);
        when(type2a.getName()).thenReturn(PokemonTypeName.UNDEFINED);

        when(p2.getType1()).thenReturn(type1b);
        when(p2.getType2()).thenReturn(type2b);
        when(type1b.getName()).thenReturn(PokemonTypeName.WATER);
        when(type2b.getName()).thenReturn(PokemonTypeName.ELECTRIC);

        // resistances union size → e.g., {FIRE, WATER, GRASS} = 3
        Set<PokemonTypeName> res1 = EnumSet.of(PokemonTypeName.FIRE, PokemonTypeName.WATER);
        Set<PokemonTypeName> res2 = EnumSet.of(PokemonTypeName.GRASS);
        when(p1.getResistances()).thenReturn(res1);
        when(p2.getResistances()).thenReturn(res2);

        // weaknesses map (with overlaps) for commonWeaknesses
        // p1: {ROCK}, p2: {ELECTRIC, GRASS} → counts: ROCK=1, ELECTRIC=1, GRASS=1 → total=3, num=3 → avg=1
        Set<PokemonTypeName> wk1 = EnumSet.of(PokemonTypeName.ROCK);
        Set<PokemonTypeName> wk2 = EnumSet.of(PokemonTypeName.ELECTRIC, PokemonTypeName.GRASS);
        when(p1.getWeaknesses()).thenReturn(wk1);
        when(p2.getWeaknesses()).thenReturn(wk2);

        when(team.getCoding()).thenReturn(new PokemonGA[]{p1, p2});

        // expected pieces
        double HIGH = 1.5d;
        double NORMAL = 1.0d;

        // averageTeamStats: (min(650,600) + 400) / 2 = 500 → normalize to [175..600]
        double avgRaw = (600 + 400) / 2.0;
        double avg = normalize(avgRaw, 175.0, 600.0, 0.0, 100.0);

        // typesDiversity: FIRE, WATER, ELECTRIC → 3 unique types → normalize [1..12]
        double types = normalize(3.0, 1.0, 12.0, 0.0, 100.0);

        // teamResistances: union size = 3 → normalize [1..18]
        double res = normalize(3.0, 1.0, 18.0, 0.0, 100.0);

        // legendaryCount: LEGENDARY=+2, COMMON=+0 → count=2 → normalize with (minX=6, maxX=0)
        double legendary = normalize(2.0, 6.0, 0.0, 0.0, 100.0);

        // commonWeaknesses: total=3, num=3 → avg=1; normalize (x=1, minX=total=3, maxX=1)
        // Denominator negative → valid (inverted scale)
        double commonW = normalize(1.0, 3.0, 1.0, 0.0, 100.0);

        double expected = HIGH * avg
                + NORMAL * types
                + NORMAL * res
                + NORMAL * legendary
                + HIGH * commonW;

        // when
        fitnessFunction.evaluate(team);

        // then
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(team).setFitness(captor.capture());

        // Tight tolerance because computations are deterministic
        assertThat(captor.getValue()).isCloseTo(expected, within(1e-9));
    }

    @Test
    @DisplayName("evaluate_ShouldComputeFitness_WhenExactlyOneMegaEvolution")
        // Boundary for mega evolution rule: exactly one Mega is allowed and should compute normally (not forced to zero)
    void evaluate_ShouldComputeFitness_WhenExactlyOneMegaEvolution() {
        // given
        when(p1.isMegaEvolution()).thenReturn(true);   // exactly one
        when(p2.isMegaEvolution()).thenReturn(false);

        // Keep everything very small but valid to avoid division-by-zero in commonWeaknesses
        when(p1.getTotal()).thenReturn(300);
        when(p2.getTotal()).thenReturn(300);

        when(p1.getRarity()).thenReturn(PokemonRarity.COMMON);
        when(p2.getRarity()).thenReturn(PokemonRarity.COMMON);

        when(p1.getType1()).thenReturn(type1a);
        when(p1.getType2()).thenReturn(type2a);
        when(p2.getType1()).thenReturn(type1b);
        when(p2.getType2()).thenReturn(type2b);
        when(type1a.getName()).thenReturn(PokemonTypeName.FIRE);
        when(type2a.getName()).thenReturn(PokemonTypeName.UNDEFINED);
        when(type1b.getName()).thenReturn(PokemonTypeName.WATER);
        when(type2b.getName()).thenReturn(PokemonTypeName.UNDEFINED);

        when(p1.getResistances()).thenReturn(EnumSet.of(PokemonTypeName.FIRE));
        when(p2.getResistances()).thenReturn(EnumSet.of(PokemonTypeName.WATER));

        when(p1.getWeaknesses()).thenReturn(EnumSet.of(PokemonTypeName.ELECTRIC));
        when(p2.getWeaknesses()).thenReturn(EnumSet.of(PokemonTypeName.GRASS));

        when(team.getCoding()).thenReturn(new PokemonGA[]{p1, p2});

        // when
        fitnessFunction.evaluate(team);

        // then: should compute some positive value (not zero due to early-return)
        ArgumentCaptor<Double> captor = ArgumentCaptor.forClass(Double.class);
        verify(team).setFitness(captor.capture());
        assertThat(captor.getValue()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("evaluate_ShouldThrowArithmeticException_WhenAllWeaknessesEmpty")
        // Edge-case safety: if every Pokemon has an empty weaknesses set, commonWeaknesses() divides by zero.
        // We assert the exception is propagated (current implementation does not guard this case).
    void evaluate_ShouldThrowArithmeticException_WhenAllWeaknessesEmpty() {
        // given (no Megas; keep other fields simple)
        when(p1.isMegaEvolution()).thenReturn(false);
        when(p2.isMegaEvolution()).thenReturn(false);

        when(p1.getTotal()).thenReturn(300);
        when(p2.getTotal()).thenReturn(300);

        when(p1.getRarity()).thenReturn(PokemonRarity.COMMON);
        when(p2.getRarity()).thenReturn(PokemonRarity.COMMON);

        when(p1.getType1()).thenReturn(type1a);
        when(p1.getType2()).thenReturn(type2a);
        when(p2.getType1()).thenReturn(type1b);
        when(p2.getType2()).thenReturn(type2b);
        when(type1a.getName()).thenReturn(PokemonTypeName.FIRE);
        when(type2a.getName()).thenReturn(PokemonTypeName.UNDEFINED);
        when(type1b.getName()).thenReturn(PokemonTypeName.WATER);
        when(type2b.getName()).thenReturn(PokemonTypeName.UNDEFINED);

        when(p1.getResistances()).thenReturn(EnumSet.of(PokemonTypeName.FIRE));
        when(p2.getResistances()).thenReturn(EnumSet.of(PokemonTypeName.WATER));

        // The trigger: empty weaknesses for every Pokemon → num == 0 in commonWeaknesses()
        when(p1.getWeaknesses()).thenReturn(EnumSet.noneOf(PokemonTypeName.class));
        when(p2.getWeaknesses()).thenReturn(EnumSet.noneOf(PokemonTypeName.class));

        when(team.getCoding()).thenReturn(new PokemonGA[]{p1, p2});

        // when / then
        assertThatThrownBy(() -> fitnessFunction.evaluate(team))
                .isInstanceOf(ArithmeticException.class);

        verify(team, never()).setFitness(anyDouble());
    }

    @Test
    @DisplayName("legendaryCount_ShouldHandleAllRarityBranchesCorrectly")
    void legendaryCount_ShouldHandleAllRarityBranchesCorrectly() throws Exception {
        // Accesso via reflection perché il metodo è privato
        var method = PokemonTeamFitnessFunction.class.getDeclaredMethod("legendaryCount", PokemonTeamGA.class);
        method.setAccessible(true);

        // quattro Pokémon con diverse rarità
        PokemonGA legendary = mock(PokemonGA.class);
        PokemonGA mythical = mock(PokemonGA.class);
        PokemonGA subLegendary = mock(PokemonGA.class);
        PokemonGA paradox = mock(PokemonGA.class);

        when(legendary.getRarity()).thenReturn(PokemonRarity.LEGENDARY);
        when(mythical.getRarity()).thenReturn(PokemonRarity.MYTHICAL);
        when(subLegendary.getRarity()).thenReturn(PokemonRarity.SUB_LEGENDARY);
        when(paradox.getRarity()).thenReturn(PokemonRarity.PARADOX);

        PokemonTeamGA team = mock(PokemonTeamGA.class);
        when(team.getCoding()).thenReturn(new PokemonGA[]{legendary, mythical, subLegendary, paradox});

        // invoke private method
        double result = (double) method.invoke(fitnessFunction, team);

        // Verifica che siano stati contati correttamente:
        // LEGENDARY +2, MYTHICAL +2, SUB_LEGENDARY +1, PARADOX +1 → totale = 6
        // normalizeFitness(6, 6, 0, 0, 100) = 0
        assertThat(result).isCloseTo(0.0, within(1e-9));
    }

    @Test
    @DisplayName("averageTeamStats should call getTotal twice when equal to MAX_TOTAL_STATS_STANDARD")
    void averageTeamStats_ShouldCallGetTotalTwice_WhenEqualToBoundary() throws Exception {
        // Access the private method using reflection
        var method = PokemonTeamFitnessFunction.class
                .getDeclaredMethod("averageTeamStats", PokemonTeamGA.class);
        method.setAccessible(true);

        // Mock a Pokémon whose total is exactly 600 on first call,
        // but returns 123 on second call to expose the else-branch behavior.
        PokemonGA pokemon = mock(PokemonGA.class);
        when(pokemon.getTotal()).thenReturn(PokemonGA.MAX_TOTAL_STATS_STANDARD, 123);

        // Mock the team containing only this Pokémon
        PokemonTeamGA team = mock(PokemonTeamGA.class);
        when(team.getCoding()).thenReturn(new PokemonGA[]{pokemon});

        // Call the private method
        double result = (double) method.invoke(fitnessFunction, team);

        // For the real code: else branch (== 600) → adds 123 → normalizeFitness(123) = 0
        assertThat(result).isEqualTo(0.0);

        // Verify getTotal() was called twice (condition + else branch)
        verify(pokemon, times(2)).getTotal();
    }

    @Test
    @DisplayName("commonWeaknesses_ShouldNormalizeBelow100_WhenTwoTypesCountedTwice")
    void commonWeaknesses_ShouldNormalizeBelow100_WhenTwoTypesCountedTwice() throws Exception {
        // Access the private method
        var method = PokemonTeamFitnessFunction.class
                .getDeclaredMethod("commonWeaknesses", PokemonTeamGA.class);
        method.setAccessible(true);

        // Two Pokémon, both weak to FIRE and WATER:
        // counts -> FIRE=2, WATER=2  => total=4, num=2
        // total/num = 2 (INTEGER division)
        PokemonGA p1 = mock(PokemonGA.class);
        PokemonGA p2 = mock(PokemonGA.class);
        when(p1.getWeaknesses()).thenReturn(EnumSet.of(PokemonTypeName.FIRE, PokemonTypeName.WATER));
        when(p2.getWeaknesses()).thenReturn(EnumSet.of(PokemonTypeName.FIRE, PokemonTypeName.WATER));

        PokemonTeamGA team = mock(PokemonTeamGA.class);
        when(team.getCoding()).thenReturn(new PokemonGA[]{p1, p2});

        double result = (double) method.invoke(fitnessFunction, team);

        // Expected using the SAME formula as production:
        // x = total/num = 2  (int division)
        // normalizeFitness(x=2, minX=total=4, maxX=1, 0..100)
        double total = 4.0;
        double num = 2.0;
        double x = 2.0; // 4/2 (int)
        double normalized = (x - total) / (1.0 - total) * 100.0;
        double expected = Math.max(0.0, Math.min(100.0, normalized)); // should be 66.666...

        assertThat(result).isCloseTo(expected, org.assertj.core.data.Offset.offset(1e-9));
        assertThat(result).isLessThan(100.0); // ensures not clamped at upper bound
    }

    @Test
    void normalizeFitness_ShouldMapMidRange_NoClamp_KillsPlusMinusMutants_A() throws Exception {
        var m = PokemonTeamFitnessFunction.class.getDeclaredMethod(
                "normalizeFitness", double.class, double.class, double.class, double.class, double.class);
        m.setAccessible(true);

        // Caso ben centrato: niente clamp e tutti i termini incidono
        double x = 25.0;
        double minX = 10.0;
        double maxX = 70.0;
        double minY = 30.0;
        double maxY = 130.0;

        double result = (double) m.invoke(fitnessFunction, x, minX, maxX, minY, maxY);

        // Expected: (25-10)/(70-10) * (130-30) + 30 = (15/60)*100 + 30 = 25 + 30 = 55
        double expected = 55.0;
        assertThat(result).isCloseTo(expected, offset(1e-9));
    }

    @Test
    void normalizeFitness_ShouldDetectSignErrorOnMinY_Term_NoClamp_KillsPlusMinusMutants_D() throws Exception {
        var m = PokemonTeamFitnessFunction.class.getDeclaredMethod(
                "normalizeFitness", double.class, double.class, double.class, double.class, double.class);
        m.setAccessible(true);

        // Qui facciamo in modo che il termine "+ minY" conti molto
        double x = 40.0;
        double minX = 0.0;
        double maxX = 80.0;
        double minY = 20.0;
        double maxY = 60.0;

        double result = (double) m.invoke(fitnessFunction, x, minX, maxX, minY, maxY);

        // Expected: (40-0)/(80-0) * (60-20) + 20 = (40/80)*40 + 20 = 0.5*40 + 20 = 40
        double expected = 40.0;
        assertThat(result).isCloseTo(expected, offset(1e-9));
    }

    @Test
    void normalizeFitness_ShouldHandleAsymmetricRanges_NoClamp_KillsPlusMinusMutants_BC() throws Exception {
        var m = PokemonTeamFitnessFunction.class.getDeclaredMethod(
                "normalizeFitness", double.class, double.class, double.class, double.class, double.class);
        m.setAccessible(true);

        // Range asimmetrici per colpire (maxX - minX) e (maxY - minY)
        double x = 12.5;
        double minX = 2.5;
        double maxX = 42.5;   // diff = 40.0, somma = 45.0 → mutante cambia parecchio
        double minY = -10.0;
        double maxY = 50.0;   // diff = 60.0

        double result = (double) m.invoke(fitnessFunction, x, minX, maxX, minY, maxY);

        // Expected: (12.5 - 2.5)/(42.5 - 2.5) * (50 - (-10)) + (-10)
        //         = (10 / 40) * 60 - 10
        //         = 0.25 * 60 - 10 = 15 - 10 = 5
        double expected = 5.0;
        assertThat(result).isCloseTo(expected, offset(1e-9));
    }
}
