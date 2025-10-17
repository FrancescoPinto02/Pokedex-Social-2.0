import { ArrowDown, MoveVertical, ArrowUp } from "lucide-react";
import OptionCard from "../OptionCard/OptionCard";
import styles from "./HeightFilter.module.scss";

type HeightOption = "short" | "medium" | "tall";

interface HeightFilterProps {
  selected: HeightOption | null;
  onChange: (value: HeightOption) => void;
  disabled?: boolean;
}

const icons: Record<HeightOption, React.ReactNode> = {
  short: <ArrowDown size={28} />,
  medium: <MoveVertical size={28} />,
  tall: <ArrowUp size={28} />,
};

const HeightFilter: React.FC<HeightFilterProps> = ({
  selected,
  onChange,
  disabled = false,
}) => {
  const options: HeightOption[] = ["short", "medium", "tall"];

  return (
    <div className={styles.filterGroup}>
      <h4>Height</h4>
      <div className={styles.optionsGrid}>
        {options.map((opt) => (
          <OptionCard
            key={opt}
            label={opt}
            icon={icons[opt]}
            selected={selected === opt}
            onClick={() => onChange(opt)}
            disabled={disabled}
          />
        ))}
      </div>
    </div>
  );
};

export default HeightFilter;
