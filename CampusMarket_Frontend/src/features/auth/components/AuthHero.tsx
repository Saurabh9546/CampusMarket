import { Logo } from '@/components/Logo/Logo';
import styles from './AuthHero.module.css';

/** Compact horizontal logo + tagline banner, shared by auth pages that
 * don't need the full LoginHero (marketing copy + value props) — just
 * enough branding to keep every auth screen visually consistent. */
export function AuthHero() {
  return (
    <div className={styles.row}>
      <Logo size="md" light />
      <p className={styles.tagline}>Your campus marketplace.</p>
    </div>
  );
}