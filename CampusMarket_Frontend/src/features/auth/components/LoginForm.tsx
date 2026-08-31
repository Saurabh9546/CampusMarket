import { useState, type FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { useAuth } from '@/hooks/useAuth';
import { validateLoginForm, hasErrors } from '../validation';
import type { LoginFormValues, FormErrors } from '../types';
import { ROUTES } from '@/constants/routes';
import { AuthFooterLink } from './AuthFooterLink';
import styles from './LoginForm.module.css';

/**
 * Note: the login endpoint doesn't distinguish "wrong password" from "correct
 * password, not verified yet" in its response shape. This form shows whatever
 * message the backend returns as-is.
 */
export function LoginForm() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [values, setValues] = useState<LoginFormValues>({ email: '', password: '' });
  const [errors, setErrors] = useState<FormErrors>({});
  const [formError, setFormError] = useState<string | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(undefined);

    const fieldErrors = validateLoginForm(values);
    setErrors(fieldErrors);
    if (hasErrors(fieldErrors)) return;

    setIsSubmitting(true);
    const result = await login(values.email, values.password);
    setIsSubmitting(false);

    if (result.success) {
      navigate(ROUTES.home);
    } else {
      setFormError(result.message);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        id="login-email"
        label="College email"
        type="email"
        placeholder="name@college.edu"
        value={values.email}
        error={errors.email}
        onChange={(e) => setValues((v) => ({ ...v, email: e.target.value }))}
      />
      <Input
        id="login-password"
        label="Password"
        type="password"
        placeholder="Enter your password"
        value={values.password}
        error={errors.password}
        onChange={(e) => setValues((v) => ({ ...v, password: e.target.value }))}
      />
      <div className={styles.forgotRow}>
        <Link to={ROUTES.forgotPassword} className={styles.forgotLink}>Forgot password?</Link>
      </div>

      <FormError message={formError} />

      <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 14 }}>
        Log in
      </Button>

      <AuthFooterLink prompt="Don't have an account?" linkText="Register" to={ROUTES.register} />
    </form>
  );
}