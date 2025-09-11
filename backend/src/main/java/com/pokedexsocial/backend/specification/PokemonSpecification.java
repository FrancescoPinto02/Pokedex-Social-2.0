package com.pokedexsocial.backend.specification;


import com.pokedexsocial.backend.controller.PokemonSearchCriteria;
import com.pokedexsocial.backend.model.Pokemon;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to build {@link Specification} objects for {@link Pokemon}.
 * <p>
 * Creates dynamic predicates from {@link PokemonSearchCriteria} and applies
 * LEFT fetch joins for associations (only on non-count queries) to avoid N+1 issues.
 */
public final class PokemonSpecification {

    private PokemonSpecification() {}

    /**
     * Builds a {@link Specification} from the given search criteria.
     *
     * @param c - the search criteria
     * @return a JPA {@link Specification} for filtering Pokémon
     */
    public static Specification<Pokemon> fromCriteria(PokemonSearchCriteria c) {
        return (root, query, cb) -> {
            boolean isCount = Long.class.equals(query.getResultType());
            if (!isCount) {
                // fetch per evitare N+1
                root.fetch("type1", JoinType.LEFT);
                root.fetch("type2", JoinType.LEFT);
                root.fetch("ability1", JoinType.LEFT);
                root.fetch("ability2", JoinType.LEFT);
                root.fetch("hiddenAbility", JoinType.LEFT);
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            // q: species substring (case-insensitive)
            if (c.getQ() != null && !c.getQ().isBlank()) {
                String pattern = "%" + c.getQ().toLowerCase().trim() + "%";
                predicates.add(cb.like(cb.lower(root.get("species")), pattern));
            }

            // types: up to 2 types, semantics -> inclusive OR
            if (c.getTypeIds() != null && !c.getTypeIds().isEmpty()) {
                List<Predicate> typePreds = new ArrayList<>();
                for (Integer typeId : c.getTypeIds()) {
                    Predicate p1 = cb.equal(root.get("type1").get("id"), typeId);
                    Predicate p2 = cb.equal(root.get("type2").get("id"), typeId);
                    typePreds.add(cb.or(p1, p2));
                }
                // any selected type matches
                predicates.add(cb.or(typePreds.toArray(new Predicate[0])));
            }

            // ability: matches any ability slot
            if (c.getAbilityId() != null) {
                Predicate a1 = cb.equal(root.get("ability1").get("id"), c.getAbilityId());
                Predicate a2 = cb.equal(root.get("ability2").get("id"), c.getAbilityId());
                Predicate ah = cb.equal(root.get("hiddenAbility").get("id"), c.getAbilityId());
                predicates.add(cb.or(a1, a2, ah));
            }

            // ndex range
            if (c.getNdexFrom() != null && c.getNdexTo() != null) {
                predicates.add(cb.between(root.get("ndex"), c.getNdexFrom(), c.getNdexTo()));
            } else if (c.getNdexFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ndex"), c.getNdexFrom()));
            } else if (c.getNdexTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ndex"), c.getNdexTo()));
            }

            // height range (BigDecimal)
            if (c.getHeightFrom() != null && c.getHeightTo() != null) {
                predicates.add(cb.between(root.get("height"), c.getHeightFrom(), c.getHeightTo()));
            } else if (c.getHeightFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("height"), c.getHeightFrom()));
            } else if (c.getHeightTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("height"), c.getHeightTo()));
            }

            // weight range
            if (c.getWeightFrom() != null && c.getWeightTo() != null) {
                predicates.add(cb.between(root.get("weight"), c.getWeightFrom(), c.getWeightTo()));
            } else if (c.getWeightFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("weight"), c.getWeightFrom()));
            } else if (c.getWeightTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("weight"), c.getWeightTo()));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
