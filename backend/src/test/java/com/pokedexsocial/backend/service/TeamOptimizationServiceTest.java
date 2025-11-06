package com.pokedexsocial.backend.service;
import com.pokedexsocial.backend.dto.OptimizationResultDTO;
import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.metaheuristics.PokemonGeneticAlgorithm;
import com.pokedexsocial.backend.service.TeamOptimizationService;
import com.pokedexsocial.backend.optimizer.ga.results.Results;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TeamOptimizationService}.
 */
@ExtendWith(MockitoExtension.class)
class TeamOptimizationServiceTest {

    @Mock
    private PokemonGeneticAlgorithm pokemonGeneticAlgorithm;

    @InjectMocks
    private TeamOptimizationService teamOptimizationService;

    @Mock
    private Results<PokemonTeamGA> resultsMock;

    @Mock
    private PokemonTeamGA bestTeamMock;

    // --- Happy path ---

    /**
     * Ensures the service maps GA results to OptimizationResultDTO correctly
     * when the algorithm returns a valid best individual and metadata.
     */
    @Test
    void optimize_ShouldReturnDTOWithValues_WhenAlgorithmReturnsValidResults() throws Exception {
        // Arrange
        double expectedFitness = 123.45;
        int expectedIterations = 7;
        List<String> expectedLog = List.of("start", "progress", "end");

        when(pokemonGeneticAlgorithm.run()).thenReturn(resultsMock);
        when(resultsMock.getBestIndividual()).thenReturn(bestTeamMock);
        when(bestTeamMock.getFitness()).thenReturn(expectedFitness);
        when(resultsMock.getNumberOfIterations()).thenReturn(expectedIterations);
        when(resultsMock.getLog()).thenReturn(expectedLog);

        // Act
        OptimizationResultDTO dto = teamOptimizationService.optimize();

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getBestTeam()).isSameAs(bestTeamMock);
        assertThat(dto.getBestFitness()).isEqualTo(expectedFitness);
        assertThat(dto.getIterations()).isEqualTo(expectedIterations);
        assertThat(dto.getLog()).containsExactlyElementsOf(expectedLog);

        // Verify interactions for mutation robustness
        verify(pokemonGeneticAlgorithm, times(1)).run();
        verify(resultsMock, times(1)).getBestIndividual();
        verify(bestTeamMock, times(1)).getFitness();
        verify(resultsMock, times(1)).getNumberOfIterations();
        verify(resultsMock, times(1)).getLog();
        verifyNoMoreInteractions(pokemonGeneticAlgorithm, resultsMock, bestTeamMock);
    }

    // --- Exceptional / edge cases ---

    /**
     * Verifies that a CloneNotSupportedException thrown by the underlying algorithm
     * is propagated by the service (method declares throws).
     */
    @Test
    void optimize_ShouldPropagateCloneNotSupportedException_WhenAlgorithmRunThrows() throws Exception {
        // Arrange
        CloneNotSupportedException expected = new CloneNotSupportedException("cloning not supported");
        when(pokemonGeneticAlgorithm.run()).thenThrow(expected);

        // Act + Assert
        assertThatThrownBy(() -> teamOptimizationService.optimize())
                .isInstanceOf(CloneNotSupportedException.class)
                .isSameAs(expected);

        verify(pokemonGeneticAlgorithm, times(1)).run();
        verifyNoMoreInteractions(pokemonGeneticAlgorithm);
        verifyNoInteractions(resultsMock, bestTeamMock);
    }

    /**
     * Ensures a NullPointerException occurs if the GA returns a null best individual,
     * because the service dereferences it to obtain fitness.
     */
    @Test
    void optimize_ShouldThrowNullPointerException_WhenBestIndividualIsNull() throws Exception {
        // Arrange
        when(pokemonGeneticAlgorithm.run()).thenReturn(resultsMock);
        when(resultsMock.getBestIndividual()).thenReturn(null);

        // Act + Assert
        assertThatThrownBy(() -> teamOptimizationService.optimize())
                .isInstanceOf(NullPointerException.class);

        verify(pokemonGeneticAlgorithm, times(1)).run();
        verify(resultsMock, times(1)).getBestIndividual();
        verifyNoMoreInteractions(pokemonGeneticAlgorithm, resultsMock);
        verifyNoInteractions(bestTeamMock);
    }
}
