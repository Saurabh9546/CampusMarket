import { useState, type InputHTMLAttributes, type ReactNode } from 'react';
import styles from './Input.module.css';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  /** Required, not derived from `label` — two inputs with the same label text
   *  on one page used to silently collide on a shared DOM id. */
  id: string;
  label: string;
  error?: string;
  icon?: ReactNode;
}

export function Input({ label, error, icon, className, id, type, ...rest }: InputProps) {
  const isPassword = type === 'password';
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className={styles.wrapper}>
      <label className={styles.label} htmlFor={id}>{label}</label>
      <div className={`${styles.inputRow} ${error ? styles.inputRowError : ''}`}>
        {icon && <span className={styles.iconSlot}>{icon}</span>}
        <input
          id={id}
          type={isPassword ? (showPassword ? 'text' : 'password') : type}
          className={[styles.input, className].filter(Boolean).join(' ')}
          {...rest}
        />
        {isPassword && (
          <button
            type="button"
            className={styles.togglePassword}
            onClick={() => setShowPassword((v) => !v)}
            aria-label={showPassword ? 'Hide password' : 'Show password'}
            tabIndex={-1}
          >
            {showPassword ? '🙈' : '👁️'}
          </button>
        )}
      </div>
      {error && <span className={styles.errorText}>{error}</span>}
    </div>
  );
}