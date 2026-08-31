import { useState, type FormEvent } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import * as authApi from '@/api/auth';
import { ROUTES } from '@/constants/routes';
import { AuthLayout } from '@/layouts/AuthLayout';
import styles from '../pages/ForgotPasswordPage.module.css';

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<string | undefined>();

  if (!token) {
    return (
      <AuthLayout>
        <div className={styles.center}>
          <div className={`${styles.iconCircle} ${styles.errorCircle}`}>!</div>
          <h2>Invalid link</h2>
          <p className="subtle">This password reset link is missing its token. Request a new one.</p>
          <Button onClick={() => navigate(ROUTES.forgotPassword)} style={{ marginTop: 16 }}>
            Request a new link
          </Button>
        </div>
      </AuthLayout>
    );
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(undefined);

    if (newPassword.length < 8) {
      setFormError('Password must be at least 8 characters');
      return;
    }
    if (newPassword !== confirmPassword) {
      setFormError('Passwords do not match');
      return;
    }

    setIsSubmitting(true);
    const result = await authApi.resetPassword(token, newPassword);
    setIsSubmitting(false);

    if (result.success) {
      navigate(ROUTES.login);
    } else {
      setFormError(result.message);
    }
  };

  return (
    <AuthLayout>
      <h2>Set a new password</h2>
      <form onSubmit={handleSubmit} noValidate>
        <Input
          id="reset-new-password"
          label="New password"
          type="password"
          placeholder="Enter new password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
        <Input
          id="reset-confirm-password"
          label="Confirm new password"
          type="password"
          placeholder="Re-enter new password"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <FormError message={formError} />
        <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 14 }}>
          Reset password
        </Button>
      </form>
    </AuthLayout>
  );
}