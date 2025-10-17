import { Feather, Scale, Weight } from "lucide-react";
import OptionCard from "../OptionCard/OptionCard";
import styles from "./WeightFilter.module.scss";

type WeightOption = "light" | "medium" | "heavy";

interface WeightFilterProps {
  selected: WeightOption | null;
  onChange: (value: WeightOption) => void;
  disabled?: boolean;
}

const icons: Record<WeightOption, React.ReactNode> = {
  light: <Feather size={28} />,
  medium: <Scale size={28} />,
  heavy: <Weight size={28} />,
};

const WeightFilter: React.FC<WeightFilterProps> = ({
  selected,
  onChange,
  disabled = false,
}) => {
  const options: WeightOption[] = ["light", "medium", "heavy"];

  return (
    <div className={styles.filterGroup}>
      <h4>Weight</h4>
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

export default WeightFilter;
