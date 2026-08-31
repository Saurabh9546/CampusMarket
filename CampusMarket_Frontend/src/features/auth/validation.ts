import type { LoginFormValues, RegisterFormValues, FormErrors } from './types';

/**
 * Basic email format check. The actual college-domain match happens
 * server-side (College table) — this client-side check is UX-only (fail
 * fast, friendly message) and is NOT the security boundary. The backend
 * re-validates on /auth/register regardless.
 */
const COLLEGE_DOMAIN_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function validateEmailFormat(email: string): string | undefined {
  if (!email) return 'College email is required';
  if (!COLLEGE_DOMAIN_PATTERN.test(email)) return 'Enter a valid email address';
  return undefined;
}

export function validatePassword(password: string): string | undefined {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Password must be at least 8 characters';
  return undefined;
}

export function validateLoginForm(values: LoginFormValues): FormErrors {
  return {
    email: validateEmailFormat(values.email),
    password: values.password ? undefined : 'Password is required',
  };
}

export function validateRegisterForm(values: RegisterFormValues): FormErrors {
  const errors: FormErrors = {
    name: values.name.trim() ? undefined : 'Full name is required',
    email: validateEmailFormat(values.email),
    password: validatePassword(values.password),
  };
  if (values.password && values.confirmPassword !== values.password) {
    errors.confirmPassword = 'Passwords do not match';
  }
  return errors;
}

export function hasErrors(errors: FormErrors): boolean {
  return Object.values(errors).some((e) => e != null);
}