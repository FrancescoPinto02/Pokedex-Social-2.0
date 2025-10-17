import styles from "./TypeFilter.module.scss";
import TypeTag from "../../Text/Type/TypeTag";
import type { FilterOption } from "../../../types/filters/FilterOption";


interface TypeFilterProps {
  types: FilterOption[];
  selectedTypes: number[];
  onChange: (selected: number[]) => void;
  maxSelected?: number;
}

const TypeFilter: React.FC<TypeFilterProps> = ({
  types,
  selectedTypes,
  onChange,
  maxSelected = 2,
}) => {
  const handleToggle = (id: number) => {
    if (selectedTypes.includes(id)) {
      onChange(selectedTypes.filter((t) => t !== id));
    } else if (selectedTypes.length < maxSelected) {
      onChange([...selectedTypes, id]);
    }
  };

  return (
    <div className={styles.filterGroup}>
      <h4>Types (max {maxSelected})</h4>
      <div className={styles.typeTagsContainer}>
        {types.map((t) => (
          <TypeTag
            key={t.id}
            type={t.name}
            selected={selectedTypes.includes(t.id)}
            onClick={() => handleToggle(t.id)}
            clickable
          />
        ))}
      </div>
    </div>
  );
};

export default TypeFilter;
