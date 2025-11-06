package com.pokedexsocial.backend.optimizer.pokemon.type;

import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.model.TypeEffectiveness;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypePool;
import com.pokedexsocial.backend.repository.TypeEffectivenessRepository;
import com.pokedexsocial.backend.repository.TypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokemonTypePoolTest {

    @Mock
    private TypeRepository typeRepository;

    @Mock
    private TypeEffectivenessRepository typeEffectivenessRepository;

    @InjectMocks
    private PokemonTypePool pool;

    private Type fire;
    private Type grass;
    private Type water;

    @BeforeEach
    void setUp() {
        fire = new Type(1, "Fire");
        grass = new Type(2, "Grass");
        water = new Type(3, "Water");
    }

    // Helper to create a TypeEffectiveness entry
    private static TypeEffectiveness te(Type attacker, Type defender, double multiplier) {
        return new TypeEffectiveness(attacker, defender, BigDecimal.valueOf(multiplier));
    }

    /**
     * Ensures that init() populates the set with all types and allows lookup by enum name
     * when repositories return consistent data (with at least one effectiveness relation).
     */
    @Test
    void init_ShouldPopulateTypesAndAllowLookup_WhenRepositoriesReturnData() {
        // given
        when(typeRepository.findAll()).thenReturn(List.of(fire, grass));
        // Fire -> Grass = 2.0 (one sample relation to exercise offensive/defensive map building)
        when(typeEffectivenessRepository.findAll()).thenReturn(List.of(
                te(fire, grass, 2.0)
        ));

        // when
        pool.init();

        // then
        Set<PokemonType> types = pool.getTypes();
        assertThat(types).isNotNull();
        assertThat(types).hasSize(2);

        // lookup by enum (valueOf(FIRE/GRASS) is exercised inside the class)
        assertThat(pool.getTypeByName(PokemonTypeName.FIRE)).isPresent();
        assertThat(pool.getTypeByName(PokemonTypeName.GRASS)).isPresent();

        // not present type -> empty
        assertThat(pool.getTypeByName(PokemonTypeName.WATER)).isEmpty();

        // sanity: names in returned set correspond to FIRE and GRASS
        assertThat(types.stream().map(PokemonType::getName))
                .containsExactlyInAnyOrder(PokemonTypeName.FIRE, PokemonTypeName.GRASS);
    }

    /**
     * Verifies the branch where there are types but NO effectiveness rows.
     * The pool must still create a PokemonType for each Type.
     */
    @Test
    void init_ShouldCreateEntries_WhenNoEffectivenessRelationsExist() {
        // given
        when(typeRepository.findAll()).thenReturn(List.of(fire, water));
        when(typeEffectivenessRepository.findAll()).thenReturn(List.of()); // no relations

        // when
        pool.init();

        // then
        Set<PokemonType> types = pool.getTypes();
        assertThat(types).isNotNull();
        assertThat(types).hasSize(2);
        assertThat(types.stream().map(PokemonType::getName))
                .containsExactlyInAnyOrder(PokemonTypeName.FIRE, PokemonTypeName.WATER);

        // getTypeByName present/absent checks
        assertThat(pool.getTypeByName(PokemonTypeName.FIRE)).isPresent();
        assertThat(pool.getTypeByName(PokemonTypeName.WATER)).isPresent();
        assertThat(pool.getTypeByName(PokemonTypeName.GRASS)).isEmpty();
    }

    /**
     * Ensures getTypeByName returns empty when the requested enum is not among loaded types.
     */
    @Test
    void getTypeByName_ShouldReturnEmpty_WhenTypeNotLoaded() {
        // given
        when(typeRepository.findAll()).thenReturn(List.of(fire)); // only FIRE
        when(typeEffectivenessRepository.findAll()).thenReturn(List.of());

        // when
        pool.init();

        // then
        assertThat(pool.getTypeByName(PokemonTypeName.GRASS)).isEmpty();
    }

    /**
     * Exercises the error branch where a DB Type name cannot be mapped to PokemonTypeName enum,
     * causing valueOf(...) to throw an IllegalArgumentException.
     */
    @Test
    void init_ShouldThrowException_WhenTypeNameNotInEnum() {
        // given: a type name that certainly doesn't exist in the enum
        Type invalid = new Type(99, "NotARealType");
        when(typeRepository.findAll()).thenReturn(List.of(invalid));
        when(typeEffectivenessRepository.findAll()).thenReturn(List.of());

        // when / then
        assertThatThrownBy(pool::init)
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Verifies that getTypes() returns the same set that was initialized and is non-null.
     */
    @Test
    void getTypes_ShouldReturnInitializedSet_WhenAfterInit() {
        // given
        when(typeRepository.findAll()).thenReturn(List.of(fire, grass, water));
        when(typeEffectivenessRepository.findAll()).thenReturn(List.of(
                te(water, fire, 2.0),
                te(grass, water, 2.0)
        ));

        // when
        pool.init();

        // then
        Set<PokemonType> types = pool.getTypes();
        assertThat(types).isNotNull().hasSize(3);
        assertThat(types.stream().map(PokemonType::getName))
                .containsExactlyInAnyOrder(PokemonTypeName.FIRE, PokemonTypeName.GRASS, PokemonTypeName.WATER);
    }
}

