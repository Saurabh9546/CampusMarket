import { Logo } from '@/components/Logo/Logo';
import styles from './AuthHero.module.css';

/** Compact logo + tagline hero, shared by auth pages that don't need the
 * full LoginHero (marketing copy + value props) — just enough branding to
 * keep every auth screen visually consistent. */
export function AuthHero() {
  return (
    <div>
      <Logo size="md" light />
      <p className={styles.tagline}>Your campus marketplace.</p>
    </div>
  );
}