import styles from "./NdexFilter.module.scss";

interface NdexFilterProps {
  value: { min: number; max: number };
  range: { min: number; max: number };
  onChange: (value: { min: number; max: number }) => void;
  disabled?: boolean;
}

const NdexFilter: React.FC<NdexFilterProps> = ({
  value,
  range,
  onChange,
  disabled = false,
}) => {
  return (
    <div className={styles.ndexFilter}>
      <label className={styles.label}>NDex Range:</label>
      <div className={styles.inputsContainer}>
        <input
          type="number"
          min={1}
          max={value.max}
          value={value.min}
          onChange={(e) =>
            onChange({ ...value, min: Math.max(1, Number(e.target.value)) })
          }
          disabled={disabled}
          className={styles.input}
        />
        <span className={styles.separator}> - </span>
        <input
          type="number"
          min={value.min}
          max={range.max}
          value={value.max}
          onChange={(e) =>
            onChange({ ...value, max: Number(e.target.value) })
          }
          disabled={disabled}
          className={styles.input}
        />
      </div>
    </div>
  );
};

export default NdexFilter;
