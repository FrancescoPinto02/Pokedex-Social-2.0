import { useRef, useState, useEffect } from 'react';
import styles from './Pokedex.module.scss';
import Title from '../../components/Text/Title/Title';
import Card from '../../components/Catalogue/Card/Card';
import FiltersMenu from '../../components/FiltersMenu/FiltersMenu';
import type { Pokemon } from '../../types/Pokemon';
import type { Filters } from '../../types/filters/Filters';
const API_URL = import.meta.env.VITE_API_URL;

interface AppliedFilters {
  selectedTypes: number[];
  selectedAbility: number | null;
  selectedWeight: 'light' | 'medium' | 'heavy' | null;
  selectedHeight: 'short' | 'medium' | 'tall' | null;
  query: string;
  ndexRange: { min: number; max: number } | null;
}

const Pokedex = () => {
  const [pokemonList, setPokemonList] = useState<Pokemon[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  const [filters, setFilters] = useState<Filters | null>(null);

  // Filtri selezionati (NON ancora Applicati)
  const [selectedTypes, setSelectedTypes] = useState<number[]>([]);
  const [selectedAbility, setSelectedAbility] = useState<number | null>(null);
  const [selectedWeight, setSelectedWeight] = useState<'light' | 'medium' | 'heavy' | null>(null);
  const [selectedHeight, setSelectedHeight] = useState<'short' | 'medium' | 'tall' | null>(null);
  const [ndexRangeSelected, setNdexRangeSelected] = useState<{ min: number; max: number }>({min: 0, max: 0});
  const [query, setQuery] = useState('');

  // Filtri applicati
  const [appliedFilters, setAppliedFilters] = useState<AppliedFilters>({
    selectedTypes: [],
    selectedAbility: null,
    selectedWeight: null,
    selectedHeight: null,
    query: '',
    ndexRange: null
  });

  const mounted = useRef(false);

  // Fetch filtri dal backend
  useEffect(() => {
    const fetchFilterOptions = async () => {
      try {
        const res = await fetch(`${API_URL}/pokemon/filters`);
        const data: Filters = await res.json();
        setFilters(data);
        setNdexRangeSelected({
          min: data.ndexRange.min,
          max: data.ndexRange.max,
      });
      } catch (err) {
        console.error('Errore nel fetch dei filtri:', err);
      }
    };
    fetchFilterOptions();
  }, []);

  // Query Builder Dinamico
  const buildQueryParams = (pageNumber: number, filtersToUse = appliedFilters) => {
    //Nessun Filtro
    if (!filters) return '';
    const params = new URLSearchParams();

    // Query
    if (filtersToUse.query) params.append('q', filtersToUse.query);

    // Types
    filtersToUse.selectedTypes.forEach(id => params.append('typeIds', id.toString()));
    if (filtersToUse.selectedAbility) params.append('abilityId', filtersToUse.selectedAbility.toString());

    // Peso
    if (filtersToUse.selectedWeight) {
      const { min, max } = filters.weightRange;
      const third = (max - min) / 3;
      switch (filtersToUse.selectedWeight) {
        case 'light':
          params.append('weightFrom', min.toString());
          params.append('weightTo', (min + third).toString());
          break;
        case 'medium':
          params.append('weightFrom', (min + third).toString());
          params.append('weightTo', (min + 2 * third).toString());
          break;
        case 'heavy':
          params.append('weightFrom', (min + 2 * third).toString());
          params.append('weightTo', max.toString());
          break;
      }
    }

    // Altezza
    if (filtersToUse.selectedHeight) {
      const { min, max } = filters.heightRange;
      const third = (max - min) / 3;
      switch (filtersToUse.selectedHeight) {
        case 'short':
          params.append('heightFrom', min.toString());
          params.append('heightTo', (min + third).toString());
          break;
        case 'medium':
          params.append('heightFrom', (min + third).toString());
          params.append('heightTo', (min + 2 * third).toString());
          break;
        case 'tall':
          params.append('heightFrom', (min + 2 * third).toString());
          params.append('heightTo', max.toString());
          break;
      }
    }

    // Dex Number
    if (filtersToUse.ndexRange) {
      params.append('ndexFrom', filtersToUse.ndexRange.min.toString());
      params.append('ndexTo', filtersToUse.ndexRange.max.toString());
    } else {
      params.append('ndexFrom', filters.ndexRange.min.toString());
      params.append('ndexTo', filters.ndexRange.max.toString());
    }

    // Paginazione e ordinamento
    params.append('page', pageNumber.toString());
    params.append('size', '12');
    params.append('sort', 'ndex,asc');

    return params.toString();
  };

  // Fetch Pokémon
  const fetchPokemon = async (
    pageNumber = 0,
    resetList = false,
    filtersToUse = appliedFilters
  ) => {
    setLoading(true);
    try {
      const queryString = buildQueryParams(pageNumber, filtersToUse);
      const response = await fetch(`${API_URL}/pokemon?${queryString}`);
      const data = await response.json();
      setPokemonList(prev => (resetList ? data.items : [...prev, ...data.items]));
      setPage(data.page);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('Errore nel recupero dei Pokémon:', error);
    } finally {
      setLoading(false);
    }
  };

  // Caricamento iniziale
  useEffect(() => {
    if (!mounted.current) {
      fetchPokemon(0, true);
      mounted.current = true;
    }
  }, []);

  // Applica filtri
  const applyFilters = () => {
    const newFilters: AppliedFilters = {
      selectedTypes,
      selectedAbility,
      selectedWeight,
      selectedHeight,
      query,
      ndexRange: ndexRangeSelected
    };
    setAppliedFilters(newFilters);
    fetchPokemon(0, true, newFilters);
  };

  // Reset controlli
  const resetFilters = () => {
    setSelectedTypes([]);
    setSelectedAbility(null);
    setSelectedWeight(null);
    setSelectedHeight(null);
    setQuery('');
    if (filters) {
      setNdexRangeSelected({
        min: filters.ndexRange.min,
        max: filters.ndexRange.max,
      });
  }
  };

  return (
    <div className={styles.pokedex}>
      <Title text="Pokèdex" />

      {/* Menu Filtri */}
      {filters && (
        <FiltersMenu
          filters={filters}
          selectedTypes={selectedTypes}
          selectedAbility={selectedAbility}
          selectedWeight={selectedWeight}
          selectedHeight={selectedHeight}
          query={query}
          ndexRangeSelected={ndexRangeSelected}
          onNdexChange={setNdexRangeSelected}
          onChange={(field, value) => {
            switch (field) {
              case 'selectedTypes': setSelectedTypes(value); break;
              case 'selectedAbility': setSelectedAbility(value); break;
              case 'selectedWeight': setSelectedWeight(value); break;
              case 'selectedHeight': setSelectedHeight(value); break;
              case 'query': setQuery(value); break;
            }
          }}
          onApply={applyFilters}
          onReset={resetFilters}
          loading={loading}
        />
      )}

      {/* Card Pokémon */}
      <div className={styles.cardContainer}>
        {pokemonList.map(p => (
          <Card
            key={p.id}
            ndex={p.ndex}
            name={p.species}
            type1={p.types[0]?.name || ''}
            type2={p.types[1]?.name || ''}
            imageUrl={`images/pokemon/${p.imageUrl}`}
          />
        ))}
      </div>

      {/* Carica altri */}
      {page + 1 < totalPages && (
        <button
          className={styles.loadMoreButton}
          onClick={() => fetchPokemon(page + 1)}
          disabled={loading}
        >
          {loading ? 'Loading' : 'Load More'}
        </button>
      )}
    </div>
  );
};

export default Pokedex;

