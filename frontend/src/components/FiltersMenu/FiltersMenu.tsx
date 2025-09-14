import React, { useState } from "react";
import styles from "./FiltersMenu.module.scss";
import SearchBar from "./SearchBar/SearchBar";
import AbilityFilter from "./AbilityFilter/AbilityFilter";
import WeightFilter from "./WeightFilter/WeightFilter";
import HeightFilter from "./HeightFilter/HeightFilter";
import TypeFilter from "./TypeFilter/TypeFilter";
import { ChevronDown, ChevronUp } from "lucide-react";
import NdexFilter from "./NdexFilter/NdexFilter";
import type { Filters } from "../../types/filters/Filters";


interface FiltersMenuProps {
  filters: Filters;
  selectedTypes: number[];
  selectedAbility: number | null;
  selectedWeight: "light" | "medium" | "heavy" | null;
  selectedHeight: "short" | "medium" | "tall" | null;
  query: string;
  ndexRangeSelected: { min: number; max: number }; // nuovo
  onNdexChange: (range: { min: number; max: number }) => void; // nuovo
  onChange: (field: string, value: any) => void;
  onApply: () => void;
  onReset: () => void;
  loading: boolean;
}

const FiltersMenu: React.FC<FiltersMenuProps> = ({
  filters,
  selectedTypes,
  selectedAbility,
  selectedWeight,
  selectedHeight,
  query,
  ndexRangeSelected,
  onNdexChange,
  onChange,
  onApply,
  onReset,
  loading,
}) => {
  const [isOpen, setIsOpen] = useState(false);

  // wrapper to close filter after search
  const handleApply = () => {
    onApply();
    setIsOpen(false);
  };

  return (
    <div className={styles.filters}>
      {/* Search bar */}
      <SearchBar
        value={query}
        placeholder="Search..."
        onChange={(val) => onChange("query", val)}
        onSearch={handleApply}
        disabled={loading}
      />

      {/* Toggle button (top when closed) */}
      {!isOpen && (
        <button
          className={styles.toggleButton}
          onClick={() => setIsOpen(true)}
        >
          <ChevronDown size={40} />
        </button>
      )}

      {/* Collapsible content */}
      <div
        className={`${styles.advancedFilters} ${
          isOpen ? styles.open : styles.closed
        }`}
      >
        {/* NDex filter */}
        <NdexFilter
          value={ndexRangeSelected}
          range={filters.ndexRange}
          onChange={onNdexChange}
          disabled={loading}
        />

        {/* Type filter */}
        <TypeFilter
          types={filters.types}
          selectedTypes={selectedTypes}
          onChange={(val) => onChange("selectedTypes", val)}
          maxSelected={2}
        />

        {/* Ability filter */}
        <AbilityFilter
          abilities={filters.abilities}
          selectedAbility={selectedAbility}
          onChange={(val) => onChange("selectedAbility", val)}
          disabled={loading}
        />

        {/* Weight filter */}
        <WeightFilter
          selected={selectedWeight}
          onChange={(val) => onChange("selectedWeight", val)}
          disabled={loading}
        />

        {/* Height filter */}
        <HeightFilter
          selected={selectedHeight}
          onChange={(val) => onChange("selectedHeight", val)}
          disabled={loading}
        />

        {/* Buttons Apply / Reset */}
        <div className={styles.filterButtons}>
          <button onClick={handleApply} disabled={loading}>
            Apply Filters
          </button>
          <button onClick={onReset} disabled={loading}>
            Reset Filters
          </button>
        </div>

        {/* Toggle button (bottom when open) */}
        <button
          className={styles.toggleButton}
          onClick={() => setIsOpen(false)}
        >
          <ChevronUp size={40} />
        </button>
      </div>
    </div>
  );
};

export default FiltersMenu;