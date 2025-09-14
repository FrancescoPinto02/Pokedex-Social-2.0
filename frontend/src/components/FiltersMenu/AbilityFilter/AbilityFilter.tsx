import React from "react";
import styles from "./AbilityFilter.module.scss";

interface AbilityOption {
  id: number;
  name: string;
}

interface AbilityFilterProps {
  abilities: AbilityOption[];
  selectedAbility: number | null;
  onChange: (ability: number | null) => void;
  disabled?: boolean;
}

const AbilityFilter: React.FC<AbilityFilterProps> = ({
  abilities,
  selectedAbility,
  onChange,
  disabled = false,
}) => {
  return (
    <div className={styles.filterGroup}>
      <h4>Ability</h4>
      <select
        value={selectedAbility ?? ""}
        onChange={(e) =>
          onChange(e.target.value ? Number(e.target.value) : null)
        }
        className={styles.selectInput}
        disabled={disabled}
      >
        <option value="">All</option>
        {abilities.map((a) => (
          <option key={a.id} value={a.id}>
            {a.name}
          </option>
        ))}
      </select>
    </div>
  );
};

export default AbilityFilter;
