package com.pokedexsocial.backend.service;

import com.pokedexsocial.backend.dto.PokemonDto;
import com.pokedexsocial.backend.dto.PokemonFiltersDto;
import com.pokedexsocial.backend.dto.PokemonListDto;
import com.pokedexsocial.backend.model.Ability;
import com.pokedexsocial.backend.model.Pokemon;
import com.pokedexsocial.backend.model.Type;
import com.pokedexsocial.backend.model.TypeEffectiveness;
import com.pokedexsocial.backend.repository.AbilityRepository;
import com.pokedexsocial.backend.repository.PokemonRepository;
import com.pokedexsocial.backend.repository.TypeEffectivenessRepository;
import com.pokedexsocial.backend.repository.TypeRepository;
import com.pokedexsocial.backend.service.PokemonService;
import com.pokedexsocial.backend.specification.PokemonSearchCriteria;
import com.pokedexsocial.backend.specification.PokemonSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonServiceTest {

    @Mock private PokemonRepository pokemonRepository;
    @Mock private TypeRepository typeRepository;
    @Mock private AbilityRepository abilityRepository;
    @Mock private TypeEffectivenessRepository effectivenessRepository;

    @InjectMocks private PokemonService service;

    // ---------- getPokemonById ----------

    /** Ensures a RuntimeException is thrown when the repository does not find the Pokémon. */
    @Test
    void getPokemonById_ShouldThrowException_WhenPokemonNotFound() {
        Integer id = 999;
        when(pokemonRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPokemonById(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Pokemon not found with id 999");

        verify(pokemonRepository).findById(id);
        verifyNoInteractions(typeRepository, abilityRepository, effectivenessRepository);
    }

    /** Ensures when type1 is null, dto.type1 remains null and effectiveness is queried with null (branch coverage). */
    @Test
    void getPokemonById_ShouldNotSetType1_WhenType1IsNull() {
        Pokemon p = basePokemon();
        p.setId(10);
        p.setType1(null); // exercise false branch
        p.setType2(null);
        p.setAbility1(new Ability(1, "A", "D")); // avoid NPEs on required fields

        when(pokemonRepository.findById(10)).thenReturn(Optional.of(p));
        // Service unconditionally calls repo with type1, which is null here; return empty list to avoid NPE on addAll
        when(effectivenessRepository.findByDefenderType(null)).thenReturn(Collections.emptyList());

        PokemonDto dto = service.getPokemonById(10);

        assertThat(dto.getType1()).isNull();

        verify(pokemonRepository).findById(10);
        verify(effectivenessRepository).findByDefenderType(null); // expect exactly this call
        verifyNoMoreInteractions(effectivenessRepository);
    }

    /** Ensures when ability1 is null, dto.ability1 remains null (branch coverage). */
    @Test
    void getPokemonById_ShouldNotSetAbility1_WhenAbility1IsNull() {
        Pokemon p = basePokemon();
        p.setId(11);
        p.setType1(new Type(1, "Normal"));
        p.setAbility1(null); // exercise false branch

        when(pokemonRepository.findById(11)).thenReturn(Optional.of(p));
        when(effectivenessRepository.findByDefenderType(any())).thenReturn(Collections.emptyList());

        PokemonDto dto = service.getPokemonById(11);

        assertThat(dto.getAbility1()).isNull();
        verify(pokemonRepository).findById(11);
        verify(effectivenessRepository).findByDefenderType(p.getType1());
    }


    /** Maps all fields and merges multipliers across two defender types; validates weaknesses/resistances/neutral classification. */
    @Test
    void getPokemonById_ShouldMapAllFields_WhenAllOptionalsPresent_AndTwoTypesWithMergedMultipliers() {
        // Arrange entity (with all optional fields)
        Pokemon p = basePokemon();
        p.setId(1);
        p.setNdex(25);
        p.setSpecies("Pikachu");
        p.setForme("Base");
        p.setDex1("Dex1");
        p.setDex2("Dex2");
        p.setHp(35);
        p.setAttack(55);
        p.setDefense(40);
        p.setSpattack(50);
        p.setSpdefense(50);
        p.setSpeed(90);
        p.setTotal(320);
        p.setWeight(new BigDecimal("6.0"));
        p.setHeight(new BigDecimal("0.4"));
        p.setPokemonClass("Mouse");
        p.setPercentMale(new BigDecimal("0.5"));
        p.setPercentFemale(new BigDecimal("0.5"));
        p.setEggGroup1("Field");
        p.setEggGroup2("Fairy");
        p.setImageUrl("http://img/pika.png");

        // Types and abilities present
        Type electric = new Type(13, "Electric");
        Type fairy = new Type(18, "Fairy");
        p.setType1(electric);
        p.setType2(fairy);
        Ability a1 = new Ability(1, "Static", "May paralyze on contact");
        Ability a2 = new Ability(2, "Lightning Rod", "Draws in all Electric-type moves");
        Ability ah = new Ability(3, "Surge Surfer", "Doubles Speed on Electric Terrain");
        p.setAbility1(a1);
        p.setAbility2(a2);
        p.setHiddenAbility(ah);

        when(pokemonRepository.findById(1)).thenReturn(Optional.of(p));

        // Effectiveness entries (attackers vs two defender types)
        // Attacker Fire: 2.0 vs Fairy, 1.0 vs Electric -> total 2.0 (weakness)
        // Attacker Ground: 2.0 vs Electric, 1.0 vs Fairy -> total 2.0 (weakness)
        // Attacker Electric: 1.0 vs Fairy, 0.5 vs Electric -> total 0.5 (resistance)
        // Attacker Normal: 1.0 vs Fairy, 1.0 vs Electric -> total 1.0 (neutral)
        Type fire = new Type(10, "Fire");
        Type ground = new Type(5, "Ground");
        Type normal = new Type(1, "Normal");

        List<TypeEffectiveness> effType1 = Arrays.asList(
                new TypeEffectiveness(fire, electric, new BigDecimal("1.0")),
                new TypeEffectiveness(ground, electric, new BigDecimal("2.0")),
                new TypeEffectiveness(new Type(13, "Electric"), electric, new BigDecimal("0.5")),
                new TypeEffectiveness(normal, electric, new BigDecimal("1.0"))
        );
        List<TypeEffectiveness> effType2 = Arrays.asList(
                new TypeEffectiveness(fire, fairy, new BigDecimal("2.0")),
                new TypeEffectiveness(ground, fairy, new BigDecimal("1.0")),
                new TypeEffectiveness(new Type(13, "Electric"), fairy, new BigDecimal("1.0")),
                new TypeEffectiveness(normal, fairy, new BigDecimal("1.0"))
        );

        when(effectivenessRepository.findByDefenderType(electric)).thenReturn(effType1);
        when(effectivenessRepository.findByDefenderType(fairy)).thenReturn(effType2);

        // Act
        PokemonDto dto = service.getPokemonById(1);

        // Assert: basic fields
        assertThat(dto.getId()).isEqualTo(1);
        assertThat(dto.getNdex()).isEqualTo(25);
        assertThat(dto.getSpecies()).isEqualTo("Pikachu");
        assertThat(dto.getForme()).isEqualTo("Base");
        assertThat(dto.getDex1()).isEqualTo("Dex1");
        assertThat(dto.getDex2()).isEqualTo("Dex2");
        assertThat(dto.getHp()).isEqualTo(35);
        assertThat(dto.getAttack()).isEqualTo(55);
        assertThat(dto.getDefense()).isEqualTo(40);
        assertThat(dto.getSpattack()).isEqualTo(50);
        assertThat(dto.getSpdefense()).isEqualTo(50);
        assertThat(dto.getSpeed()).isEqualTo(90);
        assertThat(dto.getTotal()).isEqualTo(320);
        assertThat(dto.getWeight()).isEqualTo(6.0);
        assertThat(dto.getHeight()).isEqualTo(0.4);
        assertThat(dto.getPokemonClass()).isEqualTo("Mouse");
        assertThat(dto.getPercentMale()).isEqualTo(0.5);
        assertThat(dto.getPercentFemale()).isEqualTo(0.5);
        assertThat(dto.getEggGroup1()).isEqualTo("Field");
        assertThat(dto.getEggGroup2()).isEqualTo("Fairy");
        assertThat(dto.getImageUrl()).isEqualTo("http://img/pika.png");

        // Types & abilities
        assertThat(dto.getType1()).isNotNull();
        assertThat(dto.getType1().id()).isEqualTo(13);
        assertThat(dto.getType1().name()).isEqualTo("Electric");
        assertThat(dto.getType2()).isNotNull();
        assertThat(dto.getType2().id()).isEqualTo(18);
        assertThat(dto.getType2().name()).isEqualTo("Fairy");

        assertThat(dto.getAbility1()).isNotNull();
        assertThat(dto.getAbility1().id()).isEqualTo(1);
        assertThat(dto.getAbility2()).isNotNull();
        assertThat(dto.getHiddenAbility()).isNotNull();

        // Multipliers classification
        assertThat(dto.getWeaknesses()).containsEntry("Fire", 2.0).containsEntry("Ground", 2.0);
        assertThat(dto.getResistances()).containsEntry("Electric", 0.5);
        assertThat(dto.getNeutral()).containsEntry("Normal", 1.0);

        // Interactions
        verify(pokemonRepository).findById(1);
        verify(effectivenessRepository).findByDefenderType(electric);
        verify(effectivenessRepository).findByDefenderType(fairy);
        verifyNoMoreInteractions(pokemonRepository, effectivenessRepository);
        verifyNoInteractions(typeRepository, abilityRepository);
    }

    /** Handles nullables and absence of type2/secondary abilities; zero multiplier (immunity) must not appear in resistances (since filter >0 && <1). */
    @Test
    void getPokemonById_ShouldHandleNullablesAndNoType2_WhenMissingOptionals() {
        Pokemon p = basePokemon();
        p.setId(2);
        p.setNdex(1);
        p.setSpecies("Bulbasaur");
        p.setForme(null);
        p.setDex1("d1");
        p.setDex2("d2");
        p.setHp(45);
        p.setAttack(49);
        p.setDefense(49);
        p.setSpattack(65);
        p.setSpdefense(65);
        p.setSpeed(45);
        p.setTotal(318);
        p.setWeight(null);
        p.setHeight(null);
        p.setPokemonClass("Seed");
        p.setPercentMale(null);
        p.setPercentFemale(null);
        p.setEggGroup1("Monster");
        p.setEggGroup2(null);
        p.setImageUrl(null);

        Type grass = new Type(12, "Grass");
        p.setType1(grass);
        p.setType2(null);
        Ability a1 = new Ability(10, "Overgrow", "Boosts Grass moves");
        p.setAbility1(a1);
        p.setAbility2(null);
        p.setHiddenAbility(null);

        when(pokemonRepository.findById(2)).thenReturn(Optional.of(p));

        // Effectiveness only for type1
        // Poison (0.0 immunity example for this test's logic) -> should NOT appear in resistances
        // Water 0.5 -> resistance; Rock 1.0 -> neutral
        Type poison = new Type(4, "Poison");
        Type water = new Type(11, "Water");
        Type rock = new Type(6, "Rock");
        List<TypeEffectiveness> eff = Arrays.asList(
                new TypeEffectiveness(poison, grass, new BigDecimal("0.0")),
                new TypeEffectiveness(water, grass, new BigDecimal("0.5")),
                new TypeEffectiveness(rock, grass, new BigDecimal("1.0"))
        );
        when(effectivenessRepository.findByDefenderType(grass)).thenReturn(eff);

        PokemonDto dto = service.getPokemonById(2);

        // Nullable fields mapped to null/doubles not set
        assertThat(dto.getWeight()).isNull();
        assertThat(dto.getHeight()).isNull();
        assertThat(dto.getPercentMale()).isNull();
        assertThat(dto.getPercentFemale()).isNull();
        assertThat(dto.getImageUrl()).isNull();

        // No type2 / ability2 / hidden
        assertThat(dto.getType2()).isNull();
        assertThat(dto.getAbility2()).isNull();
        assertThat(dto.getHiddenAbility()).isNull();

        // Classifications
        assertThat(dto.getResistances()).containsEntry("Water", 0.5);
        assertThat(dto.getNeutral()).containsEntry("Rock", 1.0);
        assertThat(dto.getWeaknesses()).doesNotContainKey("Poison");
        assertThat(dto.getResistances()).doesNotContainKey("Poison"); // not included due to > 0.0 && < 1.0
        assertThat(dto.getNeutral()).doesNotContainKey("Poison");

        verify(pokemonRepository).findById(2);
        verify(effectivenessRepository).findByDefenderType(grass);
        verifyNoMoreInteractions(pokemonRepository, effectivenessRepository);
        verifyNoInteractions(typeRepository, abilityRepository);
    }

    // ---------- search ----------

    /** Verifies the Specification built from criteria is passed to repository and the mapping returns list entries. */
    @Test
    void search_ShouldDelegateToRepository_WithSpecificationFromCriteria() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        Pageable pageable = PageRequest.of(0, 20, Sort.by("ndex"));

        // Prepare page with mixed typing to exercise mapping branches elsewhere
        Pokemon p1 = withTypes(100, "OneTypeMon", new Type(1, "Normal"), null);
        Pokemon p2 = withTypes(101, "DualTypeMon", new Type(10, "Fire"), new Type(2, "Water"));
        Page<Pokemon> page = new PageImpl<>(List.of(p1, p2), pageable, 2);

        try (MockedStatic<PokemonSpecification> mocked = mockStatic(PokemonSpecification.class)) {
            // Return a dummy specification instance; we only need identity/any()
            @SuppressWarnings("unchecked")
            org.springframework.data.jpa.domain.Specification<com.pokedexsocial.backend.model.Pokemon> spec =
                    (root, query, cb) -> cb.conjunction();

            mocked.when(() -> PokemonSpecification.fromCriteria(criteria)).thenReturn(spec);
            when(pokemonRepository.findAll(spec, pageable)).thenReturn(page);

            Page<PokemonListDto> result = service.search(criteria, pageable);

            // Verify delegation & mapping (sizes and basic field checks)
            mocked.verify(() -> PokemonSpecification.fromCriteria(criteria));
            verify(pokemonRepository).findAll(spec, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2);
            PokemonListDto r1 = result.getContent().get(0);
            PokemonListDto r2 = result.getContent().get(1);

            assertThat(r1.id()).isEqualTo(p1.getId());
            assertThat(r1.ndex()).isEqualTo(p1.getNdex());
            assertThat(r1.species()).isEqualTo(p1.getSpecies());
            assertThat(r1.types()).hasSize(1).extracting(t -> t.name()).containsExactly("Normal");

            assertThat(r2.id()).isEqualTo(p2.getId());
            assertThat(r2.ndex()).isEqualTo(p2.getNdex());
            assertThat(r2.species()).isEqualTo(p2.getSpecies());
            assertThat(r2.types()).hasSize(2).extracting(t -> t.name()).containsExactly("Fire", "Water");
        }
    }

    /** Covers false branch where p.getType1() == null during search mapping. */
    @Test
    void search_ShouldSkipType1Mapping_WhenType1IsNull() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        Pageable pageable = Pageable.unpaged();

        Pokemon noType1 = basePokemon();
        noType1.setId(200);
        noType1.setType1(null);
        noType1.setType2(new Type(9, "Steel"));

        Page<Pokemon> page = new PageImpl<>(List.of(noType1));

        try (MockedStatic<PokemonSpecification> mocked = mockStatic(PokemonSpecification.class)) {
            @SuppressWarnings("unchecked")
            org.springframework.data.jpa.domain.Specification<com.pokedexsocial.backend.model.Pokemon> spec =
                    (root, query, cb) -> cb.conjunction();
            mocked.when(() -> PokemonSpecification.fromCriteria(criteria)).thenReturn(spec);
            when(pokemonRepository.findAll(spec, pageable)).thenReturn(page);

            Page<PokemonListDto> result = service.search(criteria, pageable);

            assertThat(result.getContent()).hasSize(1);
            PokemonListDto dto = result.getContent().get(0);
            assertThat(dto.types()).hasSize(1); // only from type2
            assertThat(dto.types().get(0).name()).isEqualTo("Steel");
        }
    }

    /** Ensures mapping branch when type2 is null results in exactly one TypeDto in the list. */
    @Test
    void search_ShouldMapOneType_WhenSecondTypeIsNull() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        Pageable pageable = Pageable.unpaged();
        Pokemon mono = withTypes(50, "Mono", new Type(3, "Flying"), null);

        Page<Pokemon> page = new PageImpl<>(List.of(mono));

        try (MockedStatic<PokemonSpecification> mocked = mockStatic(PokemonSpecification.class)) {
            @SuppressWarnings("unchecked")
            org.springframework.data.jpa.domain.Specification<com.pokedexsocial.backend.model.Pokemon> spec =
                    (root, query, cb) -> cb.conjunction();
            mocked.when(() -> PokemonSpecification.fromCriteria(criteria)).thenReturn(spec);
            when(pokemonRepository.findAll(spec, pageable)).thenReturn(page);

            Page<PokemonListDto> result = service.search(criteria, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).types()).hasSize(1).extracting(t -> t.name())
                    .containsExactly("Flying");
        }
    }

    /** Ensures mapping branch when type2 is present results in two TypeDto entries. */
    @Test
    void search_ShouldMapTwoTypes_WhenSecondTypeIsPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        Pageable pageable = Pageable.unpaged();
        Pokemon dual = withTypes(51, "Dual", new Type(8, "Ghost"), new Type(17, "Dark"));

        Page<Pokemon> page = new PageImpl<>(List.of(dual));

        try (MockedStatic<PokemonSpecification> mocked = mockStatic(PokemonSpecification.class)) {
            @SuppressWarnings("unchecked")
            org.springframework.data.jpa.domain.Specification<com.pokedexsocial.backend.model.Pokemon> spec =
                    (root, query, cb) -> cb.conjunction();
            mocked.when(() -> PokemonSpecification.fromCriteria(criteria)).thenReturn(spec);
            when(pokemonRepository.findAll(spec, pageable)).thenReturn(page);

            Page<PokemonListDto> result = service.search(criteria, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).types()).hasSize(2).extracting(t -> t.name())
                    .containsExactly("Ghost", "Dark");
        }
    }

    // ---------- getFilters ----------

    /** Asserts filters DTO is assembled with all lists and ranges gathered from repositories, including ordering/values. */
    @Test
    void getFilters_ShouldReturnAllFilters_WhenRepositoriesReturnValues() {
        // Types
        Type fire = new Type(10, "Fire");
        Type water = new Type(11, "Water");
        when(typeRepository.findAll()).thenReturn(List.of(fire, water));

        // Abilities
        Ability blaze = new Ability(1, "Blaze", "Boosts Fire moves");
        Ability torrent = new Ability(2, "Torrent", "Boosts Water moves");
        when(abilityRepository.findAll()).thenReturn(List.of(blaze, torrent));

        // Ranges
        when(pokemonRepository.findMinNdex()).thenReturn(1);
        when(pokemonRepository.findMaxNdex()).thenReturn(1025);
        when(pokemonRepository.findMinHeight()).thenReturn(new BigDecimal("0.1"));
        when(pokemonRepository.findMaxHeight()).thenReturn(new BigDecimal("20.0"));
        when(pokemonRepository.findMinWeight()).thenReturn(new BigDecimal("0.1"));
        when(pokemonRepository.findMaxWeight()).thenReturn(new BigDecimal("999.9"));

        PokemonFiltersDto filters = service.getFilters();

        // Types mapped
        assertThat(filters.types()).hasSize(2);
        assertThat(filters.types()).extracting(t -> t.id()).containsExactly(10, 11);
        assertThat(filters.types()).extracting(t -> t.name()).containsExactly("Fire", "Water");

        // Abilities mapped
        assertThat(filters.abilities()).hasSize(2);
        assertThat(filters.abilities()).extracting(a -> a.id()).containsExactly(1, 2);
        assertThat(filters.abilities()).extracting(a -> a.name()).containsExactly("Blaze", "Torrent");

        // Ranges (ensure values preserved)
        assertThat(filters.ndexRange().getMin()).isEqualTo(1);
        assertThat(filters.ndexRange().getMax()).isEqualTo(1025);
        assertThat(filters.heightRange().getMin()).isEqualTo(new BigDecimal("0.1"));
        assertThat(filters.heightRange().getMax()).isEqualTo(new BigDecimal("20.0"));
        assertThat(filters.weightRange().getMin()).isEqualTo(new BigDecimal("0.1"));
        assertThat(filters.weightRange().getMax()).isEqualTo(new BigDecimal("999.9"));

        verify(typeRepository).findAll();
        verify(abilityRepository).findAll();
        verify(pokemonRepository).findMinNdex();
        verify(pokemonRepository).findMaxNdex();
        verify(pokemonRepository).findMinHeight();
        verify(pokemonRepository).findMaxHeight();
        verify(pokemonRepository).findMinWeight();
        verify(pokemonRepository).findMaxWeight();
    }

    // ---------- helpers ----------

    private Pokemon basePokemon() {
        Pokemon p = new Pokemon();
        // set minimal required fields to avoid NPEs in service mapping
        p.setNdex(1);
        p.setSpecies("Species");
        p.setDex1("d1");
        p.setDex2("d2");
        p.setHp(1);
        p.setAttack(1);
        p.setDefense(1);
        p.setSpattack(1);
        p.setSpdefense(1);
        p.setSpeed(1);
        p.setTotal(6);
        // type1 & ability1 will be assigned in each test as required
        return p;
    }

    private Pokemon withTypes(int ndex, String species, Type t1, Type t2) {
        Pokemon p = basePokemon();
        p.setId(ndex);
        p.setNdex(ndex);
        p.setSpecies(species);
        p.setType1(t1);
        p.setType2(t2);
        p.setAbility1(new Ability(1000 + ndex, "A" + ndex, "Desc"));
        return p;
    }
}
