import styles from './Logo.module.css';

interface LogoProps {
  size?: 'sm' | 'md' | 'lg';
  light?: boolean;
}

export function Logo({ size = 'md', light = false }: LogoProps) {
  return (
    <span className={`${styles.logo} ${styles[size]}`} style={light ? { color: '#fff' } : undefined}>
      Campus<span className={styles.accent}>Market</span>
    </span>
  );
}
