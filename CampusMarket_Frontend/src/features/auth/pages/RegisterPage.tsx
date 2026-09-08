import { AuthLayout } from '@/layouts/AuthLayout';
import { RegisterForm } from '../components/RegisterForm';
import { AuthHero } from '../components/AuthHero';

export function RegisterPage() {
  return (
    <AuthLayout hero={<AuthHero />}>
      <h2>Create your account</h2>
      <p className="subtle" style={{ marginBottom: 10 }}>
        Use your college email — that's how we verify you.
      </p>
      <RegisterForm />
    </AuthLayout>
  );
}