import type { ReactNode } from 'react';
import styles from './AuthLayout.module.css';

interface AuthLayoutProps {
  hero?: ReactNode;
  /** When true, the hero always renders as a horizontal top banner with the
   * form full-width below it, at every screen size — used by every auth
   * page except Login, which keeps the side-by-side desktop layout. */
  stacked?: boolean;
  children: ReactNode;
}

/** Shared split-screen shell for Login/Register/VerifyEmail pages. */
export function AuthLayout({ hero, stacked, children }: AuthLayoutProps) {
  return (
    <div className={`${styles.wrap} ${stacked ? styles.stacked : ''}`}>
      {hero && <div className={styles.hero}>{hero}</div>}
      <div className={styles.form}>{children}</div>
    </div>
  );
}