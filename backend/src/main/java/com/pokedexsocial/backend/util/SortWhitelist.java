package com.pokedexsocial.backend.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for filtering {@link Sort} properties based on a whitelist.
 * <p>
 * Ensures that only allowed sort properties are used in queries, falling back
 * to a default sort if none of the requested properties are permitted.
 */
public final class SortWhitelist {

    private SortWhitelist() {}

    /**
     * Filters a {@link Sort} object according to a whitelist of allowed properties.
     *
     * @param input       the Sort requested by the client
     * @param allowed     the set of allowed sort properties
     * @param defaultSort the fallback Sort if input is null, unsorted, or invalid
     * @return a filtered and safe Sort object
     */
    public static Sort filter(Sort input, Set<String> allowed, Sort defaultSort) {
        if (input == null || input.isUnsorted()) {
            return defaultSort;
        }

        List<Sort.Order> safeOrders = new ArrayList<>();
        for (Sort.Order order : input) {
            if (allowed.contains(order.getProperty())) {
                safeOrders.add(order);
            }
        }

        return safeOrders.isEmpty() ? defaultSort : Sort.by(safeOrders);
    }
}
