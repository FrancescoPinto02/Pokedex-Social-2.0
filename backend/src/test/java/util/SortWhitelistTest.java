package util;

import com.pokedexsocial.backend.util.SortWhitelist;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SortWhitelistTest {

    // ################################ filter() ##########################################
    /**
     * Should return defaultSort when input is null.
     */
    @Test
    void filter_ShouldReturnDefaultSort_WhenInputIsNull() {
        // given
        Sort defaultSort = Sort.by("default");
        Set<String> allowed = Set.of("name", "type");

        // when
        Sort result = SortWhitelist.filter(null, allowed, defaultSort);

        // then
        assertThat(result).isEqualTo(defaultSort);
    }

    /**
     * Should return defaultSort when input is unsorted.
     */
    @Test
    void filter_ShouldReturnDefaultSort_WhenInputIsUnsorted() {
        // given
        Sort input = Sort.unsorted();
        Sort defaultSort = Sort.by("fallback");
        Set<String> allowed = Set.of("id", "name");

        // when
        Sort result = SortWhitelist.filter(input, allowed, defaultSort);

        // then
        assertThat(result).isEqualTo(defaultSort);
    }

    /**
     * Should return defaultSort when none of the input properties are allowed.
     */
    @Test
    void filter_ShouldReturnDefaultSort_WhenNoPropertiesAllowed() {
        // given
        Sort input = Sort.by("height", "weight");
        Sort defaultSort = Sort.by("id");
        Set<String> allowed = Set.of("name", "type");

        // when
        Sort result = SortWhitelist.filter(input, allowed, defaultSort);

        // then
        assertThat(result).isEqualTo(defaultSort);
    }

    /**
     * Should return filtered Sort containing only allowed properties.
     */
    @Test
    void filter_ShouldReturnFilteredSort_WhenSomePropertiesAllowed() {
        // given
        Sort input = Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("weight"),
                Sort.Order.asc("type")
        );
        Sort defaultSort = Sort.by("id");
        Set<String> allowed = Set.of("name", "type");

        // when
        Sort result = SortWhitelist.filter(input, allowed, defaultSort);

        // then
        assertThat(result).isNotEqualTo(defaultSort);

        List<Sort.Order> orders = result.toList(); // Explicitly typed list
        assertThat(orders)
                .hasSize(2)
                .extracting(Sort.Order::getProperty)
                .containsExactly("name", "type");
        assertThat(orders.get(0).getDirection()).isEqualTo(Sort.Direction.ASC);
        assertThat(orders.get(1).getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    /**
     * Should return input Sort unchanged when all properties are allowed.
     */
    @Test
    void filter_ShouldReturnSameSort_WhenAllPropertiesAllowed() {
        // given
        Sort input = Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("type")
        );
        Sort defaultSort = Sort.by("id");
        Set<String> allowed = Set.of("name", "type");

        // when
        Sort result = SortWhitelist.filter(input, allowed, defaultSort);

        // then
        assertThat(result).isNotEqualTo(defaultSort);
        assertThat(result.toList())
                .containsExactlyElementsOf(input.toList());
    }

    /**
     * Should return defaultSort when allowed set is empty.
     */
    @Test
    void filter_ShouldReturnDefaultSort_WhenAllowedSetIsEmpty() {
        // given
        Sort input = Sort.by("name");
        Sort defaultSort = Sort.by("id");
        Set<String> allowed = Set.of();

        // when
        Sort result = SortWhitelist.filter(input, allowed, defaultSort);

        // then
        assertThat(result).isEqualTo(defaultSort);
    }
}