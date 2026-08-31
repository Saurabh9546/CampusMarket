import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '@/components/Input/Input';
import { Button } from '@/components/Button/Button';
import { FormError } from '@/components/FormError/FormError';
import { useAuth } from '@/hooks/useAuth';
import { validateRegisterForm, hasErrors } from '../validation';
import type { RegisterFormValues, FormErrors } from '../types';
import { ROUTES } from '@/constants/routes';
import { AuthFooterLink } from './AuthFooterLink';

export function RegisterForm() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [values, setValues] = useState<RegisterFormValues>({
    name: '', email: '', password: '', confirmPassword: '',
  });
  const [errors, setErrors] = useState<FormErrors>({});
  const [formError, setFormError] = useState<string | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setFormError(undefined);

    const fieldErrors = validateRegisterForm(values);
    setErrors(fieldErrors);
    if (hasErrors(fieldErrors)) return;

    setIsSubmitting(true);
    const result = await register(values.name, values.email, values.password);
    setIsSubmitting(false);

    if (result.success) {
      navigate(ROUTES.verifyEmail, { state: { email: values.email } });
    } else {
      // Covers invalid domain / already-registered-email cases, surfaced as-is
      // from the backend since the college domain list is server-owned data.
      setFormError(result.message);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      <Input
        id="register-name"
        label="Full name"
        placeholder="Aditi Kumar"
        value={values.name}
        error={errors.name}
        onChange={(e) => setValues((v) => ({ ...v, name: e.target.value }))}
      />
      <Input
        id="register-email"
        label="College email"
        type="email"
        placeholder="name@college.edu"
        value={values.email}
        error={errors.email}
        onChange={(e) => setValues((v) => ({ ...v, email: e.target.value }))}
      />
      <Input
        id="register-password"
        label="Password"
        type="password"
        placeholder="At least 8 characters"
        value={values.password}
        error={errors.password}
        onChange={(e) => setValues((v) => ({ ...v, password: e.target.value }))}
      />
      <Input
        id="register-confirm-password"
        label="Confirm password"
        type="password"
        placeholder="Re-enter password"
        value={values.confirmPassword}
        error={errors.confirmPassword}
        onChange={(e) => setValues((v) => ({ ...v, confirmPassword: e.target.value }))}
      />

      <FormError message={formError} />

      <Button type="submit" fullWidth isLoading={isSubmitting} style={{ marginTop: 20 }}>
        Create account
      </Button>

      <AuthFooterLink prompt="Already have an account?" linkText="Log in" to={ROUTES.login} />
    </form>
  );
}