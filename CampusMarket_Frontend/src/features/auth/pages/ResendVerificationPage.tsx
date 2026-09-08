import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { AuthFooterLink } from '../components/AuthFooterLink';
import { AuthHero } from '../components/AuthHero';
import { useAuth } from '@/hooks/useAuth';
import { ROUTES } from '@/constants/routes';
import { AuthLayout } from '@/layouts/AuthLayout';
import styles from './ForgotPasswordPage.module.css';

export function ResendVerificationPage() {
  const navigate = useNavigate();
  const { resendVerification } = useAuth();
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [formError, setFormError] = useState<string | undefined>();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(undefined);
    setIsSubmitting(true);
    const result = await resendVerification(email);
    setIsSubmitting(false);

    if (result.success) {
      // Always show the same success state, regardless of whether the email
      // exists or is already verified — prevents leaking account status.
      setSubmitted(true);
    } else {
      setFormError(result.message);
    }
  };

  if (submitted) {
    return (
      <AuthLayout hero={<AuthHero />}>
        <div className={styles.center}>
          <div className={styles.iconCircle}>✉</div>
          <h2>Check your inbox</h2>
          <p className="subtle">
            If an account exists for <b>{email}</b> and isn't verified yet, we've sent a new verification link.
          </p>
          <Button onClick={() => navigate(ROUTES.login)} style={{ marginTop: 16 }}>
            Back to login
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout hero={<AuthHero />}>
      <h2>Resend verification email</h2>
      <p className="subtle" style={{ marginBottom: 10 }}>
        Enter your college email and we'll send you a new verification link.
      </p>
      <form onSubmit={handleSubmit} noValidate>
        <Input
          id="resend-email"
          label="College email"
          type="email"
          placeholder="name@college.edu"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormError message={formError} />
        <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 14 }}>
          Resend verification link
        </Button>
      </form>
      <AuthFooterLink prompt="Already verified?" linkText="Back to login" to={ROUTES.login} />
    </AuthLayout>
  );
}