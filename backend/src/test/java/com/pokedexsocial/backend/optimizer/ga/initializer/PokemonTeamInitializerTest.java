package com.pokedexsocial.backend.optimizer.ga.initializer;

import com.pokedexsocial.backend.optimizer.ga.individuals.PokemonTeamGA;
import com.pokedexsocial.backend.optimizer.ga.initializer.PokemonTeamInitializer;
import com.pokedexsocial.backend.optimizer.ga.population.FixedSizePopulation;
import com.pokedexsocial.backend.optimizer.ga.population.Population;
import com.pokedexsocial.backend.optimizer.pokemon.team.PokemonTeamGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PokemonTeamInitializerTest {

    @Mock
    private PokemonTeamGenerator pokemonTeamGenerator;

    @Mock
    private PokemonTeamGA mockTeam1;

    @Mock
    private PokemonTeamGA mockTeam2;

    @InjectMocks
    private PokemonTeamInitializer initializer;

    @BeforeEach
    void setUp() {
        // No special setup needed; @InjectMocks will initialize with the mock generator
    }

    @Test
        // Tests that initialize(int) creates a population of correct size with generated individuals
    void initialize_ShouldReturnPopulationOfGivenSize_WhenNumberOfIndividualsIsPositive() {
        // Arrange
        when(pokemonTeamGenerator.generatePokemonTeam(PokemonTeamGA.MAX_TEAM_MEMBERS))
                .thenReturn(mockTeam1, mockTeam2);

        int numberOfIndividuals = 2;

        // Act
        Population<PokemonTeamGA> population = initializer.initialize(numberOfIndividuals);

        // Assert
        assertNotNull(population);
        assertEquals(FixedSizePopulation.class, population.getClass(), "Population should be FixedSizePopulation");
        assertEquals(numberOfIndividuals, population.size(), "Population size should match input");
        assertTrue(population.contains(mockTeam1));
        assertTrue(population.contains(mockTeam2));

        // Verify generator was called correct number of times
        verify(pokemonTeamGenerator, times(numberOfIndividuals))
                .generatePokemonTeam(PokemonTeamGA.MAX_TEAM_MEMBERS);
    }

    @Test
        // Tests that initialize(int) returns an empty population when numberOfIndividuals is zero
    void initialize_ShouldReturnEmptyPopulation_WhenNumberOfIndividualsIsZero() {
        // Act
        Population<PokemonTeamGA> population = initializer.initialize(0);

        // Assert
        assertNotNull(population);
        assertEquals(0, population.size());
        verify(pokemonTeamGenerator, never()).generatePokemonTeam(anyInt());
    }

    @Test
        // Tests that initialize() delegates to initialize(100)
    void initialize_ShouldCallInitializeWithDefaultValue_WhenNoArgsProvided() {
        // Arrange
        when(pokemonTeamGenerator.generatePokemonTeam(PokemonTeamGA.MAX_TEAM_MEMBERS))
                .thenReturn(mockTeam1);

        // Act
        Population<PokemonTeamGA> population = initializer.initialize();

        // Assert
        assertNotNull(population);
        assertInstanceOf(FixedSizePopulation.class, population, "Population should be of type FixedSizePopulation");

        FixedSizePopulation<PokemonTeamGA> fixed = (FixedSizePopulation<PokemonTeamGA>) population;
        assertEquals(100, fixed.getMaxSize(), "Default maxSize should be 100");

        // Verify generator was called correct number of times
        verify(pokemonTeamGenerator, times(100))
                .generatePokemonTeam(PokemonTeamGA.MAX_TEAM_MEMBERS);
    }
}

