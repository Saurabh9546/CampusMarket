import styles from './Divider.module.css';

interface DividerProps {
  label?: string;
}

export function Divider({ label }: DividerProps) {
  if (!label) return <div className={styles.line} />;
  return (
    <div className={styles.divider}>
      <span className={styles.line} />
      {label}
      <span className={styles.line} />
    </div>
  );
}
