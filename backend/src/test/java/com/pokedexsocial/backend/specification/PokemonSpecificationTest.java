package com.pokedexsocial.backend.specification;

import com.pokedexsocial.backend.model.Pokemon;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonSpecificationTest {

    @Mock private Root<Pokemon> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;

    // Paths (raw types to avoid generics issues with Mockito & CriteriaBuilder)
    @Mock private Path speciesPath;
    @Mock private Path lowerSpeciesPath;

    @Mock private Path type1Path;
    @Mock private Path type2Path;
    @Mock private Path type1IdPath;
    @Mock private Path type2IdPath;

    @Mock private Path ability1Path;
    @Mock private Path ability2Path;
    @Mock private Path hiddenAbilityPath;
    @Mock private Path ability1IdPath;
    @Mock private Path ability2IdPath;
    @Mock private Path hiddenAbilityIdPath;

    @Mock private Path ndexPath;
    @Mock private Path heightPath;
    @Mock private Path weightPath;

    /* ---------------------------------------------------------
       EMPTY CRITERIA (conjunction)
    --------------------------------------------------------- */
    @Test
    // Tests that when all criteria are null/empty, cb.conjunction() is returned
    void fromCriteria_ShouldReturnConjunction_WhenNoCriteriaProvided() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        Predicate conjunction = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conjunction);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(conjunction, result);
        verify(cb).conjunction();
    }

    /* ---------------------------------------------------------
       q (species LIKE)
    --------------------------------------------------------- */
    @Test
    // Tests LIKE predicate creation when q is non-blank
    void fromCriteria_ShouldAddLikePredicate_WhenQIsNonBlank() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setQ(" Bulba ");

        Predicate like = mock(Predicate.class);
        Predicate and = mock(Predicate.class);

        when(root.get("species")).thenReturn(speciesPath);
        when(cb.lower(speciesPath)).thenReturn(lowerSpeciesPath);
        when(cb.like(lowerSpeciesPath, "%bulba%")).thenReturn(like);
        when(cb.and(any(Predicate[].class))).thenReturn(and);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(and, result);
    }

    @Test
        // Tests no LIKE predicate when q is blank
    void fromCriteria_ShouldNotAddLikePredicate_WhenQIsBlank() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setQ("   "); // blank

        Predicate conj = mock(Predicate.class);
        when(cb.conjunction()).thenReturn(conj);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(conj, result);
        verify(cb, never()).like(any(), anyString());
    }

    /* ---------------------------------------------------------
       typeIds (AND of ORs)
    --------------------------------------------------------- */
    @Test
    // Tests AND of OR(type1,type2) for each typeId
    void fromCriteria_ShouldAddAndOfOrPredicatesForEachType_WhenTypeIdsPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setTypeIds(Arrays.asList(10, 20));

        Predicate eq11 = mock(Predicate.class);
        Predicate eq12 = mock(Predicate.class);
        Predicate eq21 = mock(Predicate.class);
        Predicate eq22 = mock(Predicate.class);

        Predicate or1 = mock(Predicate.class);
        Predicate or2 = mock(Predicate.class);
        Predicate andTypes = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("type1")).thenReturn(type1Path);
        when(root.get("type2")).thenReturn(type2Path);
        when(type1Path.get("id")).thenReturn(type1IdPath);
        when(type2Path.get("id")).thenReturn(type2IdPath);

        when(cb.equal(type1IdPath, 10)).thenReturn(eq11);
        when(cb.equal(type2IdPath, 10)).thenReturn(eq12);
        when(cb.or(eq11, eq12)).thenReturn(or1);

        when(cb.equal(type1IdPath, 20)).thenReturn(eq21);
        when(cb.equal(type2IdPath, 20)).thenReturn(eq22);
        when(cb.or(eq21, eq22)).thenReturn(or2);

        lenient().when(cb.and(or1, or2)).thenReturn(andTypes);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    /* ---------------------------------------------------------
       abilityId (OR ability1/ability2/hidden)
    --------------------------------------------------------- */
    @Test
    // Tests OR predicate across ability1, ability2, hiddenAbility
    void fromCriteria_ShouldAddOrOfAbilityPredicates_WhenAbilityIdPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setAbilityId(42);

        Predicate a1 = mock(Predicate.class);
        Predicate a2 = mock(Predicate.class);
        Predicate ah = mock(Predicate.class);
        Predicate orAbilities = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("ability1")).thenReturn(ability1Path);
        when(root.get("ability2")).thenReturn(ability2Path);
        when(root.get("hiddenAbility")).thenReturn(hiddenAbilityPath);

        when(ability1Path.get("id")).thenReturn(ability1IdPath);
        when(ability2Path.get("id")).thenReturn(ability2IdPath);
        when(hiddenAbilityPath.get("id")).thenReturn(hiddenAbilityIdPath);

        when(cb.equal(ability1IdPath, 42)).thenReturn(a1);
        when(cb.equal(ability2IdPath, 42)).thenReturn(a2);
        when(cb.equal(hiddenAbilityIdPath, 42)).thenReturn(ah);

        when(cb.or(a1, a2, ah)).thenReturn(orAbilities);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    /* ---------------------------------------------------------
       ndex range
    --------------------------------------------------------- */
    @Test
    // Tests ndex BETWEEN when both bounds exist
    void fromCriteria_ShouldAddBetweenPredicateForNdex_WhenBothBoundsPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setNdexFrom(1);
        criteria.setNdexTo(100);

        Predicate between = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("ndex")).thenReturn(ndexPath);
        when(cb.between(ndexPath, 1, 100)).thenReturn(between);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests ndex >= when only from
    void fromCriteria_ShouldAddGtePredicateForNdex_WhenOnlyFromPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setNdexFrom(50);

        Predicate gte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("ndex")).thenReturn(ndexPath);
        when(cb.greaterThanOrEqualTo(ndexPath, 50)).thenReturn(gte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests ndex <= when only to
    void fromCriteria_ShouldAddLtePredicateForNdex_WhenOnlyToPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        criteria.setNdexTo(150);

        Predicate lte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("ndex")).thenReturn(ndexPath);
        when(cb.lessThanOrEqualTo(ndexPath, 150)).thenReturn(lte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    /* ---------------------------------------------------------
       height range
    --------------------------------------------------------- */
    @Test
    // Tests height BETWEEN
    void fromCriteria_ShouldAddBetweenPredicateForHeight_WhenBothBoundsPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal from = new BigDecimal("0.5");
        BigDecimal to = new BigDecimal("2.0");
        criteria.setHeightFrom(from);
        criteria.setHeightTo(to);

        Predicate between = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("height")).thenReturn(heightPath);
        when(cb.between(heightPath, from, to)).thenReturn(between);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests height >=
    void fromCriteria_ShouldAddGtePredicateForHeight_WhenOnlyFromPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal from = new BigDecimal("1.0");
        criteria.setHeightFrom(from);

        Predicate gte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("height")).thenReturn(heightPath);
        when(cb.greaterThanOrEqualTo(heightPath, from)).thenReturn(gte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests height <=
    void fromCriteria_ShouldAddLtePredicateForHeight_WhenOnlyToPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal to = new BigDecimal("5.0");
        criteria.setHeightTo(to);

        Predicate lte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("height")).thenReturn(heightPath);
        when(cb.lessThanOrEqualTo(heightPath, to)).thenReturn(lte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    /* ---------------------------------------------------------
       weight range
    --------------------------------------------------------- */
    @Test
    // Tests weight BETWEEN
    void fromCriteria_ShouldAddBetweenPredicateForWeight_WhenBothBoundsPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal from = new BigDecimal("10.0");
        BigDecimal to = new BigDecimal("50.0");
        criteria.setWeightFrom(from);
        criteria.setWeightTo(to);

        Predicate between = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("weight")).thenReturn(weightPath);
        when(cb.between(weightPath, from, to)).thenReturn(between);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests weight >=
    void fromCriteria_ShouldAddGtePredicateForWeight_WhenOnlyFromPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal from = new BigDecimal("25.0");
        criteria.setWeightFrom(from);

        Predicate gte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("weight")).thenReturn(weightPath);
        when(cb.greaterThanOrEqualTo(weightPath, from)).thenReturn(gte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }

    @Test
        // Tests weight <=
    void fromCriteria_ShouldAddLtePredicateForWeight_WhenOnlyToPresent() {
        PokemonSearchCriteria criteria = new PokemonSearchCriteria();
        BigDecimal to = new BigDecimal("80.0");
        criteria.setWeightTo(to);

        Predicate lte = mock(Predicate.class);
        Predicate finalAnd = mock(Predicate.class);

        when(root.get("weight")).thenReturn(weightPath);
        when(cb.lessThanOrEqualTo(weightPath, to)).thenReturn(lte);
        when(cb.and(any(Predicate[].class))).thenReturn(finalAnd);

        Specification<Pokemon> spec = PokemonSpecification.fromCriteria(criteria);
        Predicate result = spec.toPredicate(root, query, cb);

        assertSame(finalAnd, result);
    }
}
