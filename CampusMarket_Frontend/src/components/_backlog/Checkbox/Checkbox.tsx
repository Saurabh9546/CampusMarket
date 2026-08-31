import type { InputHTMLAttributes } from 'react';
import styles from './Checkbox.module.css';

interface CheckboxProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
}

export function Checkbox({ label, id, ...rest }: CheckboxProps) {
  const checkboxId = id ?? label.toLowerCase().replace(/\s+/g, '-');
  return (
    <label className={styles.wrapper} htmlFor={checkboxId}>
      <input type="checkbox" id={checkboxId} className={styles.checkbox} {...rest} />
      {label}
    </label>
  );
}
