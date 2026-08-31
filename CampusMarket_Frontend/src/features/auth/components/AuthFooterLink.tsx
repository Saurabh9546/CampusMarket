import { Link } from 'react-router-dom';
import styles from './AuthFooterLink.module.css';

interface AuthFooterLinkProps {
  prompt: string;
  linkText: string;
  to: string;
}

/** Was duplicated as `.footerRow` in both LoginForm.module.css and RegisterForm.module.css. */
export function AuthFooterLink({ prompt, linkText, to }: AuthFooterLinkProps) {
  return (
    <p className={styles.footerRow}>
      {prompt} <Link to={to}>{linkText}</Link>
    </p>
  );
}
