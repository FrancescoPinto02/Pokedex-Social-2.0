import React from "react";
import { Check } from "lucide-react";
import styles from "./OptionCard.module.scss";

interface OptionCardProps {
  label: string;
  icon: React.ReactNode;
  selected: boolean;
  onClick: () => void;
  disabled?: boolean;
}

const OptionCard: React.FC<OptionCardProps> = ({
  label,
  icon,
  selected,
  onClick,
  disabled = false,
}) => {
  return (
    <div
      className={`${styles.optionCard} ${selected ? styles.selected : ""} ${
        disabled ? styles.disabled : ""
      }`}
      onClick={!disabled ? onClick : undefined}
    >
      <div className={styles.icon}>{icon}</div>
      <span className={styles.label}>{label}</span>
      {selected && <Check className={styles.checkIcon} />}
    </div>
  );
};

export default OptionCard;
