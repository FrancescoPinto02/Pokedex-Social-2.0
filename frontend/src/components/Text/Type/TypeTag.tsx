import { Check } from "lucide-react";
import styles from "./TypeTag.module.scss";

const typeColors: Record<string, string> = {
  fire: "#E8583D",
  water: "#4A90E2",
  grass: "#4CAF50",
  poison: "#9C27B0",
  flying: "linear-gradient(to right, #87CEEB, #B0B0B0)",
  ground: "#C2B280",
  electric: "#FFD700",
  ice: "#00CED1",
  fighting: "#D2691E",
  psychic: "#FF69B4",
  rock: "#A9A9A9",
  ghost: "#6A5ACD",
  dragon: "linear-gradient(to right, #1ea0d3ff, #d45656ff)",
  dark: "#2F4F4F",
  steel: "#B0C4DE",
  fairy: "#FFB6C1",
  normal: "#C0C0C0",
  bug: "#77a740ff",
};

const DEFAULT_COLOR = "#999999";

interface TypeTagProps {
  type: string;
  selected?: boolean;
  onClick?: () => void;
  clickable?: boolean;
}

const TypeTag: React.FC<TypeTagProps> = ({
  type,
  selected = false,
  onClick,
  clickable = false,
}) => {
  const background = typeColors[type.toLowerCase()] || DEFAULT_COLOR;

  return (
    <span
      className={`
        ${styles.typeSpan} 
        ${selected ? styles.selected : ""} 
        ${clickable ? styles.clickable : ""}
      `}
      style={{ background }}
      onClick={clickable && onClick ? onClick : undefined}
    >
      <span className={styles.label}>{type}</span>
      {selected && clickable && <Check size={16} className={styles.checkIcon} />}
    </span>
  );
};

export default TypeTag;

