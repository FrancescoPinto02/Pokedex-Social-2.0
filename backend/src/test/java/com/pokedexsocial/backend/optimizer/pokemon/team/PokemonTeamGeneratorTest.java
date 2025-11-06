package com.pokedexsocial.backend.optimizer.pokemon.team;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGA;
import com.pokedexsocial.backend.optimizer.pokemon.core.PokemonGenerator;
import com.pokedexsocial.backend.optimizer.pokemon.team.PokemonTeamGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PokemonTeamGeneratorTest {

    @Mock
    private PokemonGenerator pokemonGenerator;

    @InjectMocks
    private PokemonTeamGenerator pokemonTeamGenerator;

    // --- helpers -------------------------------------------------------------

    private static PokemonGA mockMon(String toStringValue) {
        PokemonGA mon = mock(PokemonGA.class);
        when(mon.toString()).thenReturn(toStringValue);
        return mon;
    }

    // --- tests ---------------------------------------------------------------

    /** Ensures no pokemons are requested and result team is empty when size == 0 */
    @Test
    void generatePokemonTeam_ShouldReturnEmptyTeam_WhenSizeIsZero() {
        PokemonTeamGA result = pokemonTeamGenerator.generatePokemonTeam(0);

        verify(pokemonGenerator, never()).generatePokemon();
        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("PokemonTeam=[]");
    }

    /** Ensures negative size behaves like zero: no iterations, empty team, no generator calls */
    @Test
    void generatePokemonTeam_ShouldReturnEmptyTeam_WhenSizeIsNegative() {
        PokemonTeamGA result = pokemonTeamGenerator.generatePokemonTeam(-3);

        verify(pokemonGenerator, never()).generatePokemon();
        assertThat(result).isNotNull();
        assertThat(result.toString()).contains("PokemonTeam=[]");
    }

    /** Generates exactly N members in order when size > 0 */
    @Test
    void generatePokemonTeam_ShouldGenerateTeamOfGivenSize_WhenSizeIsPositive() {
        PokemonGA p1 = mockMon("#1 Bulbasaur");
        PokemonGA p2 = mockMon("#2 Ivysaur");
        PokemonGA p3 = mockMon("#3 Venusaur");

        when(pokemonGenerator.generatePokemon()).thenReturn(p1, p2, p3);

        PokemonTeamGA result = pokemonTeamGenerator.generatePokemonTeam(3);

        verify(pokemonGenerator, times(3)).generatePokemon();
        String s = result.toString();
        assertThat(s).contains("#1 Bulbasaur", "#2 Ivysaur", "#3 Venusaur");
        // order matters (robust against mutations that shuffle/skip)
        assertThat(s.indexOf("#1 Bulbasaur")).isLessThan(s.indexOf("#2 Ivysaur"));
        assertThat(s.indexOf("#2 Ivysaur")).isLessThan(s.indexOf("#3 Venusaur"));
    }

    /** Propagates null elements if the generator returns null (no filtering performed) */
    @Test
    void generatePokemonTeam_ShouldIncludeNullMembers_WhenGeneratorReturnsNull() {
        PokemonGA p1 = mockMon("#25 Pikachu");
        when(pokemonGenerator.generatePokemon()).thenReturn(p1, null);

        PokemonTeamGA result = pokemonTeamGenerator.generatePokemonTeam(2);

        verify(pokemonGenerator, times(2)).generatePokemon();
        String s = result.toString();
        assertThat(s).contains("#25 Pikachu");
        assertThat(s).contains("null");
        // ensure the null is the second entry (order preserved)
        assertThat(s.indexOf("#25 Pikachu")).isLessThan(s.indexOf("null"));
    }

    /** Confirms there is no implicit cap at MAX_TEAM_MEMBERS (method accepts any size) */
    @Test
    void generatePokemonTeam_ShouldNotCapTeamSize_WhenSizeExceedsMaxTeamMembers() {
        int requested = PokemonTeamGA.MAX_TEAM_MEMBERS + 2; // 8
        PokemonGA[] mons = new PokemonGA[requested];
        for (int i = 0; i < requested; i++) {
            mons[i] = mockMon("#" + (i + 1) + " Mon" + (i + 1));
        }
        when(pokemonGenerator.generatePokemon())
                .thenReturn(mons[0], mons[1], mons[2], mons[3], mons[4], mons[5], mons[6], mons[7]);

        PokemonTeamGA result = pokemonTeamGenerator.generatePokemonTeam(requested);

        verify(pokemonGenerator, times(requested)).generatePokemon();
        String s = result.toString();
        // sanity: includes first and last, implying array length >= requested and no cap was applied
        assertThat(s).contains("#1 Mon1");
        assertThat(s).contains("#8 Mon8");
        // additional order check for robustness
        assertThat(s.indexOf("#1 Mon1")).isLessThan(s.indexOf("#8 Mon8"));
    }
}
