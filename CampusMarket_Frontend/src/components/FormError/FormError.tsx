import styles from './FormError.module.css';

interface FormErrorProps {
  message?: string;
}

/**
 * Was duplicated verbatim in LoginForm.module.css and RegisterForm.module.css.
 * Single definition now — any future auth/marketplace form reuses this.
 */
export function FormError({ message }: FormErrorProps) {
  if (!message) return null;
  return <div className={styles.formError}>{message}</div>;
}
