import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { AuthFooterLink } from '../components/AuthFooterLink';
import * as authApi from '@/api/auth';
import { ROUTES } from '@/constants/routes';
import { AuthLayout } from '@/layouts/AuthLayout';
import styles from './ForgotPasswordPage.module.css';

export function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [formError, setFormError] = useState<string | undefined>();

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(undefined);
    setIsSubmitting(true);
    const result = await authApi.forgotPassword(email);
    setIsSubmitting(false);

    if (result.success) {
      // Always show the same success state, regardless of whether the email
      // exists — prevents leaking which addresses are registered.
      setSubmitted(true);
    } else {
      setFormError(result.message);
    }
  };

  if (submitted) {
    return (
      <AuthLayout>
        <div className={styles.center}>
          <div className={styles.iconCircle}>✉</div>
          <h2>Check your inbox</h2>
          <p className="subtle">
            If an account exists for <b>{email}</b>, we've sent a link to reset your password.
          </p>
          <Button onClick={() => navigate(ROUTES.login)} style={{ marginTop: 16 }}>
            Back to login
          </Button>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <h2>Forgot your password?</h2>
      <p className="subtle" style={{ marginBottom: 10 }}>
        Enter your college email and we'll send you a reset link.
      </p>
      <form onSubmit={handleSubmit} noValidate>
        <Input
          id="forgot-email"
          label="College email"
          type="email"
          placeholder="name@college.edu"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <FormError message={formError} />
        <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 14 }}>
          Send reset link
        </Button>
      </form>
      <AuthFooterLink prompt="Remembered it?" linkText="Back to login" to={ROUTES.login} />
    </AuthLayout>
  );
}