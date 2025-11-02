package optimizer.pokemon.pokedex;

import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonRarity;
import com.pokedexsocial.backend.optimizer.pokemon.pokedex.Pokedex;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonType;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypeName;
import com.pokedexsocial.backend.optimizer.pokemon.type.PokemonTypePool;
import com.pokedexsocial.backend.repository.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PokedexTest {

    @Mock
    private PokemonTypePool pokemonTypePool;

    @Mock
    private PokemonRepository pokemonRepository;

    @InjectMocks
    private Pokedex pokedex;

    private Map<PokemonTypeName, Double> off;
    private Map<PokemonTypeName, Double> def;
    private PokemonType FIRE;
    private PokemonType WATER;

    @BeforeEach
    void setUp() {
        off = new HashMap<>();
        off.put(PokemonTypeName.NORMAL, 1.0);
        def = new HashMap<>();
        def.put(PokemonTypeName.NORMAL, 1.0);

        FIRE = new PokemonType(PokemonTypeName.FIRE, off, def);
        WATER = new PokemonType(PokemonTypeName.WATER, off, def);
    }

    // Utility builders
    private static Pokemon dbPokemon(int ndex, String species, Type type1, Type type2,
                                     int hp, int atk, int defn, int spa, int spd, int spe) {
        Pokemon p = new Pokemon();
        p.setId(ndex);
        p.setNdex(ndex);
        p.setSpecies(species);
        p.setDex1("d1");
        p.setDex2("d2");
        p.setType1(type1);
        p.setType2(type2);
        p.setHp(hp);
        p.setAttack(atk);
        p.setDefense(defn);
        p.setSpattack(spa);
        p.setSpdefense(spd);
        p.setSpeed(spe);
        p.setTotal(hp + atk + defn + spa + spd + spe);
        p.setPokemonClass("class");
        p.setWeight(BigDecimal.ONE);
        p.setHeight(BigDecimal.ONE);
        return p;
    }

    private static Type type(String name) {
        Type t = new Type();
        t.setName(name);
        return t;
    }

    @Test
    @DisplayName("init_ShouldPopulateMapAndConvertTypes_WhenRepositoryReturnsPokemons")
    void init_ShouldPopulateMapAndConvertTypes_WhenRepositoryReturnsPokemons() {
        // Arrange mocks needed for this scenario
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.FIRE))).thenReturn(Optional.of(FIRE));
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.WATER))).thenReturn(Optional.of(WATER));
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.ICE))).thenReturn(Optional.empty());

        Pokemon mewtwoLike = dbPokemon(150, "MewtwoLike", type("Fire"), null, 90, 100, 90, 100, 90, 100);
        Pokemon articunoLike = dbPokemon(144, "ArticunoLike", type("Water"), null, 80, 85, 100, 95, 85, 85);
        Pokemon dragoniteLike = dbPokemon(149, "DragoniteLike", type("Fire"), type("Water"), 91, 134, 95, 100, 100, 80);
        Pokemon mewLike = dbPokemon(151, "MewLike", type("Ice"), type("Water"), 100, 100, 100, 100, 100, 100);
        Pokemon bulbaA = dbPokemon(1, "Bulba-A", type("Water"), null, 45, 49, 49, 65, 65, 45);
        Pokemon bulbaB = dbPokemon(1, "Bulba-B", type("Fire"), null, 46, 50, 50, 66, 66, 46);

        when(pokemonRepository.findAllWithTypes())
                .thenReturn(Arrays.asList(mewtwoLike, articunoLike, dragoniteLike, mewLike, bulbaA, bulbaB));

        // Act
        pokedex.init();

        // Assert
        Collection<PokemonGA> all = pokedex.getAllPokemons();
        assertThat(all).hasSize(6);

        assertThat(pokedex.getByNdex(150)).get().extracting(PokemonGA::getRarity)
                .isEqualTo(PokemonRarity.LEGENDARY);
        assertThat(pokedex.getByNdex(144)).get().extracting(PokemonGA::getRarity)
                .isEqualTo(PokemonRarity.SUB_LEGENDARY);
        assertThat(pokedex.getByNdex(149)).get().extracting(PokemonGA::getRarity)
                .isEqualTo(PokemonRarity.PSEUDO_LEGENDARY);
        assertThat(pokedex.getByNdex(151)).get().extracting(PokemonGA::getRarity)
                .isEqualTo(PokemonRarity.MYTHICAL);
        assertThat(pokedex.getByNdex(1)).get().extracting(PokemonGA::getRarity)
                .isEqualTo(PokemonRarity.COMMON);

        PokemonGA ndex150 = pokedex.getByNdex(150).orElseThrow();
        assertThat(ndex150.getType1().getName()).isEqualTo(PokemonTypeName.FIRE);
        assertThat(ndex150.getType2().getName()).isEqualTo(PokemonTypeName.UNDEFINED);

        PokemonGA ndex151 = pokedex.getByNdex(151).orElseThrow();
        assertThat(ndex151.getType1().getName()).isEqualTo(PokemonTypeName.WATER);
        assertThat(ndex151.getType2().getName()).isEqualTo(PokemonTypeName.UNDEFINED);

        assertThat(pokedex.getByNdex(1)).get().extracting(PokemonGA::getName)
                .isEqualTo("Bulba-A");
    }

    @Test
    @DisplayName("init_ShouldThrow_WhenBothTypesAreNullAndConstructorRejectsDoubleUndefined")
    void init_ShouldThrow_WhenBothTypesAreNullAndConstructorRejectsDoubleUndefined() {
        Pokemon invalid = dbPokemon(999, "Invalid", null, null, 1, 1, 1, 1, 1, 1);
        when(pokemonRepository.findAllWithTypes()).thenReturn(Collections.singletonList(invalid));
        assertThatThrownBy(() -> pokedex.init())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one defined type");
    }

    @Test
    @DisplayName("getAllPokemons_ShouldReturnFlattenedCollection_WhenInitialized")
    void getAllPokemons_ShouldReturnFlattenedCollection_WhenInitialized() {
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.FIRE))).thenReturn(Optional.of(FIRE));
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.WATER))).thenReturn(Optional.of(WATER));

        Pokemon a = dbPokemon(10, "A", type("Fire"), null, 1, 1, 1, 1, 1, 1);
        Pokemon b = dbPokemon(10, "B", type("Water"), null, 1, 1, 1, 1, 1, 1);
        when(pokemonRepository.findAllWithTypes()).thenReturn(Arrays.asList(a, b));

        pokedex.init();

        Collection<PokemonGA> all = pokedex.getAllPokemons();
        assertThat(all).hasSize(2)
                .extracting(PokemonGA::getName)
                .containsExactlyInAnyOrder("A", "B");
    }

    @Test
    @DisplayName("getByNdex_ShouldReturnFirstForm_WhenNdexExists")
    void getByNdex_ShouldReturnFirstForm_WhenNdexExists() {
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.FIRE))).thenReturn(Optional.of(FIRE));
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.WATER))).thenReturn(Optional.of(WATER));

        Pokemon a = dbPokemon(25, "Pika-A", type("Fire"), null, 1, 1, 1, 1, 1, 1);
        Pokemon b = dbPokemon(25, "Pika-B", type("Water"), null, 1, 1, 1, 1, 1, 1);
        when(pokemonRepository.findAllWithTypes()).thenReturn(Arrays.asList(a, b));

        pokedex.init();

        Optional<PokemonGA> result = pokedex.getByNdex(25);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Pika-A");
    }

    @Test
    @DisplayName("getByNdex_ShouldReturnEmpty_WhenNdexDoesNotExist")
    void getByNdex_ShouldReturnEmpty_WhenNdexDoesNotExist() {
        when(pokemonRepository.findAllWithTypes()).thenReturn(Collections.emptyList());
        pokedex.init();
        assertThat(pokedex.getByNdex(12345)).isEmpty();
    }

    @Test
    @DisplayName("getByNdex_ShouldReturnEmpty_WhenFormsListIsEmpty")
    void getByNdex_ShouldReturnEmpty_WhenFormsListIsEmpty() throws Exception {
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.FIRE))).thenReturn(Optional.of(FIRE));
        Pokemon a = dbPokemon(77, "SeventySeven", type("Fire"), null, 1, 1, 1, 1, 1, 1);
        when(pokemonRepository.findAllWithTypes()).thenReturn(Collections.singletonList(a));
        pokedex.init();

        Field field = Pokedex.class.getDeclaredField("pokemons");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, List<PokemonGA>> map = (Map<Integer, List<PokemonGA>>) field.get(pokedex);
        map.put(77, new ArrayList<>());

        assertThat(pokedex.getByNdex(77)).isEmpty();
    }

    @Test
    @DisplayName("getRandomPokemon_ShouldReturnAnElementFromLoadedPool_WhenPoolIsNonEmpty")
    void getRandomPokemon_ShouldReturnAnElementFromLoadedPool_WhenPoolIsNonEmpty() {
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.FIRE))).thenReturn(Optional.of(FIRE));
        when(pokemonTypePool.getTypeByName(eq(PokemonTypeName.WATER))).thenReturn(Optional.of(WATER));

        Pokemon a = dbPokemon(33, "Nidorino-A", type("Fire"), null, 1, 1, 1, 1, 1, 1);
        Pokemon b = dbPokemon(33, "Nidorino-B", type("Water"), null, 1, 1, 1, 1, 1, 1);
        Pokemon c = dbPokemon(55, "Golduck", type("Water"), null, 1, 1, 1, 1, 1, 1);
        when(pokemonRepository.findAllWithTypes()).thenReturn(Arrays.asList(a, b, c));

        pokedex.init();
        PokemonGA randomPick = pokedex.getRandomPokemon();

        assertThat(randomPick).isIn(pokedex.getAllPokemons());
    }

    @Test
    @DisplayName("convertType_ShouldReturnUndefinedType_WhenDbTypeIsNull")
    void convertType_ShouldReturnUndefinedType_WhenDbTypeIsNull() throws Exception {
        // Use reflection to access private method
        var method = Pokedex.class.getDeclaredMethod("convertType", com.pokedexsocial.backend.model.Type.class);
        method.setAccessible(true);

        // Act
        Object result = method.invoke(pokedex, new Object[]{null});

        // Assert
        assertThat(result)
                .as("convertType(null) should not return null")
                .isInstanceOf(PokemonType.class);
        assertThat(((PokemonType) result).getName())
                .isEqualTo(PokemonTypeName.UNDEFINED);
    }
}