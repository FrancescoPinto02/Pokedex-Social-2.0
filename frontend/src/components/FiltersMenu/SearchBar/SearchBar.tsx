import React from 'react';
import styles from "./SearchBar.module.scss"

interface SearchBarProps {
  value: string;
  placeholder?: string;
  onChange: (value: string) => void;
  onSearch: () => void;
  disabled?: boolean;
}

const SearchBar: React.FC<SearchBarProps> = ({
  value,
  placeholder = "Search...",
  onChange,
  onSearch,
  disabled = false,
}) => {
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Enter") {
      onSearch();
    }
  };

  return (
    <div className={styles.searchBar}>
      <input
        type="text"
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        onKeyDown={handleKeyDown}
        className={styles.searchInput}
        disabled={disabled}
      />
      <button
        onClick={onSearch}
        className={styles.searchButton}
        disabled={disabled}
      >
        Search
      </button>
    </div>
  );
};

export default SearchBar;