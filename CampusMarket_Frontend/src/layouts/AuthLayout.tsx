import type { ReactNode } from 'react';
import styles from './AuthLayout.module.css';

interface AuthLayoutProps {
  hero?: ReactNode;
  children: ReactNode;
}

/** Shared split-screen shell for Login/Register/VerifyEmail pages. */
export function AuthLayout({ hero, children }: AuthLayoutProps) {
  return (
    <div className={styles.wrap}>
      {hero && <div className={styles.hero}>{hero}</div>}
      <div className={styles.form}>{children}</div>
    </div>
  );
}
