package com.pokedexsocial.backend.optimizer.pokemon.core;

import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeMultiplier;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonGATest {

    // We keep mocks available but use them only in the tests that need them.
    @Mock
    private PokemonType mockType1;

    @Mock
    private PokemonType mockType2;

    private EnumMap<PokemonTypeName, Double> fullNormalMap() {
        EnumMap<PokemonTypeName, Double> m = new EnumMap<>(PokemonTypeName.class);
        for (PokemonTypeName n : PokemonTypeName.values()) {
            m.put(n, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS);
        }
        return m;
    }

    private PokemonType concreteType(PokemonTypeName name, Map<PokemonTypeName, Double> defOverrides) {
        EnumMap<PokemonTypeName, Double> defs = fullNormalMap();
        if (defOverrides != null) {
            defs.putAll(defOverrides);
        }
        // Offensive map is irrelevant for PokemonGA; give it the same shape
        EnumMap<PokemonTypeName, Double> offs = fullNormalMap();
        return new PokemonType(name, offs, defs);
    }

    // ----------------------------------------------------------
    // Constructors (without null types to avoid the class’s UNDEFINED empty-map NPE)
    // ----------------------------------------------------------

    /**
     * Verifies that the total is always the sum of all stats.
     */
    @Test
    void constructor_ShouldAlwaysComputeTotalAsSumOfStats() {
        PokemonType grass = concreteType(PokemonTypeName.GRASS, null);
        PokemonType poison = concreteType(PokemonTypeName.POISON, null);

        int hp = 10, atk = 10, def = 10, spa = 10, spd = 10, spe = 10;
        int expected = hp + atk + def + spa + spd + spe;

        PokemonGA pokemon = new PokemonGA(
                1, "Bulbasaur",
                grass, poison,
                hp, atk, def, spa, spd, spe,
                PokemonRarity.COMMON
        );

        // Total should always match the sum of individual stats
        assertThat(pokemon.getTotal()).isEqualTo(expected);

        // Invariance check
        int recomputed = pokemon.getHp() + pokemon.getAttack() + pokemon.getDefense()
                + pokemon.getSpecialAttack() + pokemon.getSpecialDefense() + pokemon.getSpeed();
        assertThat(pokemon.getTotal()).isEqualTo(recomputed);
    }

    /**
     * Verifies that when both types are null, an exception is thrown.
     */
    @Test
    void constructor_ShouldThrowException_WhenBothTypesAreNull() {
        assertThatThrownBy(() -> new PokemonGA(
                1, "MissingTypes",
                null, null,
                10, 10, 10, 10, 10, 10,
                PokemonRarity.COMMON
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one defined type");
    }

    /**
     * Verifies that the constructor swaps types when type1 is UNDEFINED but type2 is defined.
     */
    @Test
    void constructor_ShouldSwapTypes_WhenType1IsUndefinedAndType2IsDefined() {
        PokemonType undefined = new PokemonType(PokemonTypeName.UNDEFINED);
        PokemonType fire = new PokemonType(PokemonTypeName.FIRE);

        PokemonGA pokemon = new PokemonGA(
                999, "SwapTest",
                undefined, fire,
                10, 10, 10, 10, 10, 10,
                PokemonRarity.COMMON
        );

        // After swap, type1 must be FIRE and type2 must be UNDEFINED
        assertThat(pokemon.getType1().getName()).isEqualTo(PokemonTypeName.FIRE);
        assertThat(pokemon.getType2().getName()).isEqualTo(PokemonTypeName.UNDEFINED);
    }

    /**
     * Verifies that if type1 is defined and type2 is undefined, no swap occurs.
     */
    @Test
    void constructor_ShouldNotSwapTypes_WhenType1DefinedAndType2Undefined() {
        PokemonType fire = new PokemonType(PokemonTypeName.FIRE);
        PokemonType undefined = new PokemonType(PokemonTypeName.UNDEFINED);

        PokemonGA pokemon = new PokemonGA(
                123, "Firemon",
                fire, undefined,
                10, 10, 10, 10, 10, 10,
                PokemonRarity.COMMON
        );

        // The types should remain as provided — no swap
        assertThat(pokemon.getType1().getName()).isEqualTo(PokemonTypeName.FIRE);
        assertThat(pokemon.getType2().getName()).isEqualTo(PokemonTypeName.UNDEFINED);
    }

    /**
     * Verifies that the total is correctly computed even with varying stat values.
     */
    @Test
    void constructor_ShouldComputeCorrectTotal_WithDifferentStats() {
        PokemonType fire = new PokemonType(PokemonTypeName.FIRE);
        PokemonType flying = new PokemonType(PokemonTypeName.FLYING);

        int hp = 10, atk = 20, def = 30, spa = 40, spd = 50, spe = 60;
        int expected = hp + atk + def + spa + spd + spe;

        PokemonGA pokemon = new PokemonGA(
                6, "Charizard",
                fire, flying,
                hp, atk, def, spa, spd, spe,
                PokemonRarity.MYTHICAL
        );

        // total = sum of stats
        assertThat(pokemon.getTotal()).isEqualTo(expected);
        assertThat(pokemon.getName()).isEqualTo("Charizard");

        // Invariance check — still equals recomputed sum
        int recomputed = pokemon.getHp() + pokemon.getAttack() + pokemon.getDefense()
                + pokemon.getSpecialAttack() + pokemon.getSpecialDefense() + pokemon.getSpeed();
        assertThat(pokemon.getTotal()).isEqualTo(recomputed);
    }

    // ----------------------------------------------------------
    // calculateResistances()
    // ----------------------------------------------------------
    @Test
    void calculateResistances_shouldAddResistancesAndImmunities_WhenFinalMultiplierLessThanNormal() {
        Map<PokemonTypeName, Double> def1 = new EnumMap<>(PokemonTypeName.class);
        def1.put(PokemonTypeName.FIRE, PokemonTypeMultiplier.RESISTS);
        def1.put(PokemonTypeName.GHOST, PokemonTypeMultiplier.IMMUNE_TO);

        PokemonType fireType = new PokemonType(PokemonTypeName.FIRE, fullNormalMap(), def1);
        PokemonType undefined = new PokemonType(PokemonTypeName.UNDEFINED);

        PokemonGA p = new PokemonGA(1, "TestMonotype", fireType, undefined,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        assertThat(p.getResistances()).contains(PokemonTypeName.FIRE, PokemonTypeName.GHOST);
    }

    @Test
    void calculateResistances_shouldCombineTypes_WhenDualType() {
        Map<PokemonTypeName, Double> def1 = new EnumMap<>(PokemonTypeName.class);
        def1.put(PokemonTypeName.FIRE, PokemonTypeMultiplier.RESISTS);

        Map<PokemonTypeName, Double> def2 = new EnumMap<>(PokemonTypeName.class);
        def2.put(PokemonTypeName.FIRE, PokemonTypeMultiplier.RESISTS);
        def2.put(PokemonTypeName.WATER, PokemonTypeMultiplier.IMMUNE_TO);

        PokemonType fire = new PokemonType(PokemonTypeName.FIRE, fullNormalMap(), def1);
        PokemonType steel = new PokemonType(PokemonTypeName.STEEL, fullNormalMap(), def2);

        PokemonGA p = new PokemonGA(2, "DualMon", fire, steel,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        assertThat(p.getResistances()).contains(PokemonTypeName.FIRE, PokemonTypeName.WATER);
    }

    @Test
    void calculateResistances_ShouldIgnoreSecondTypeWhenMonotype() {
        // Forziamo monotipo
        PokemonTypeName immuneType = PokemonTypeName.ELECTRIC;

        EnumMap<PokemonTypeName, Double> def1 = fullNormalMap();
        EnumMap<PokemonTypeName, Double> def2 = fullNormalMap();
        def2.put(immuneType, PokemonTypeMultiplier.IMMUNE_TO);

        PokemonType type1 = concreteType(PokemonTypeName.NORMAL, def1);
        PokemonType type2 = new PokemonType(PokemonTypeName.UNDEFINED, fullNormalMap(), def2);

        PokemonGA p = new PokemonGA(1, "MonoCheck",
                type1, type2,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        // Se monotype = true, l’immunità di type2 deve essere ignorata
        assertThat(p.getResistances()).doesNotContain(immuneType);
    }




    // ----------------------------------------------------------
    // calculateWeaknesses()
    // ----------------------------------------------------------

    @Test
    void calculateWeaknesses_shouldAddWeakness_WhenFinalMultiplierGreaterThanNormal() {
        Map<PokemonTypeName, Double> def1 = new EnumMap<>(PokemonTypeName.class);
        def1.put(PokemonTypeName.WATER, PokemonTypeMultiplier.WEAK_TO);   // 2.0
        def1.put(PokemonTypeName.GRASS, PokemonTypeMultiplier.NORMAL_EFFECTIVENESS); // 1.0
        def1.put(PokemonTypeName.FIRE, PokemonTypeMultiplier.RESISTS);   // 0.5

        PokemonType grassType = new PokemonType(PokemonTypeName.GRASS, fullNormalMap(), def1);
        PokemonType undefined = new PokemonType(PokemonTypeName.UNDEFINED);

        PokemonGA p = new PokemonGA(100, "WeakMono", grassType, undefined,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        Set<PokemonTypeName> weak = p.getWeaknesses();

        // 2x (WATER) → incluso, 1x (GRASS) → escluso, 0.5x (FIRE) → escluso
        assertThat(weak)
                .contains(PokemonTypeName.WATER)
                .doesNotContain(PokemonTypeName.GRASS, PokemonTypeName.FIRE);
    }

    @Test
    void calculateWeaknesses_shouldCombineTypes_WhenDualType() {
        Map<PokemonTypeName, Double> def1 = new EnumMap<>(PokemonTypeName.class);
        Map<PokemonTypeName, Double> def2 = new EnumMap<>(PokemonTypeName.class);

        def1.put(PokemonTypeName.ELECTRIC, PokemonTypeMultiplier.WEAK_TO); // x2
        def2.put(PokemonTypeName.GRASS, PokemonTypeMultiplier.WEAK_TO);    // x2

        PokemonType flying = new PokemonType(PokemonTypeName.FLYING, fullNormalMap(), def1);
        PokemonType water = new PokemonType(PokemonTypeName.WATER, fullNormalMap(), def2);

        PokemonGA p = new PokemonGA(101, "DualWeak", flying, water,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        assertThat(p.getWeaknesses())
                .contains(PokemonTypeName.ELECTRIC, PokemonTypeName.GRASS);
    }

    @Test
    void calculateWeaknesses_ShouldIgnoreSecondTypeWhenMonotype() {
        // Monotype forzato
        PokemonTypeName fakeWeakness = PokemonTypeName.ELECTRIC;

        EnumMap<PokemonTypeName, Double> def1 = fullNormalMap();
        EnumMap<PokemonTypeName, Double> def2 = fullNormalMap();
        def2.put(fakeWeakness, PokemonTypeMultiplier.WEAK_TO); // 2x ma su type2 (UNDEFINED)

        PokemonType type1 = concreteType(PokemonTypeName.NORMAL, def1);
        PokemonType type2 = new PokemonType(PokemonTypeName.UNDEFINED, fullNormalMap(), def2);

        PokemonGA p = new PokemonGA(102, "MonoCheckWeak",
                type1, type2,
                10, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        // Se monotype = true, le debolezze di type2 non devono essere conteggiate
        assertThat(p.getWeaknesses()).doesNotContain(fakeWeakness);
    }

    // ----------------------------------------------------------
    // Utilities: isMegaEvolution, equals, hashCode, toString
    // (use concrete types to avoid Mockito stubbing altogether)
    // ----------------------------------------------------------

    /** isMegaEvolution true for names containing "Mega" except "Meganium". */
    @Test
    void isMegaEvolution_ShouldReturnTrue_WhenNameContainsMegaButNotMeganium() {
        PokemonType fire = concreteType(PokemonTypeName.FIRE, null);
        PokemonType water = concreteType(PokemonTypeName.WATER, null);

        PokemonGA mega = new PokemonGA(100, "MegaCharizard", fire, water,
                60, 10, 10, 10, 10, 10, PokemonRarity.COMMON);
        PokemonGA notMega = new PokemonGA(101, "Meganium", fire, water,
                60, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        assertThat(mega.isMegaEvolution()).isTrue();
        assertThat(notMega.isMegaEvolution()).isFalse();
    }

    @Test
    void isMegaEvolution_ShouldReturnFalse_WhenNameDoesNotContainMega() {
        PokemonType fire = new PokemonType(PokemonTypeName.FIRE);
        PokemonType water = new PokemonType(PokemonTypeName.WATER);

        PokemonGA normal = new PokemonGA(200, "Pikachu", fire, water,
                60, 10, 10, 10, 10, 10, PokemonRarity.COMMON);

        assertThat(normal.isMegaEvolution()).isFalse();
    }

}
