import type { ButtonHTMLAttributes, ReactNode } from 'react';
import styles from './SocialButton.module.css';

type SocialProvider = 'google' | 'microsoft' | 'apple';

interface SocialButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  provider: SocialProvider;
  icon: ReactNode;
  label?: string;
}

/**
 * Presentational only. Current auth is email + password — no OAuth2 client
 * config, no callback endpoint. Not wired into LoginPage/RegisterPage. Don't
 * render it in a real page until OAuth2 login is actually built — a button
 * that does nothing on click is worse than no button.
 */
export function SocialButton({ provider, icon, label, disabled = true, ...rest }: SocialButtonProps) {
  const defaultLabel = provider.charAt(0).toUpperCase() + provider.slice(1);
  return (
    <button type="button" className={styles.button} disabled={disabled} {...rest}>
      {icon} {label ?? defaultLabel}
    </button>
  );
}