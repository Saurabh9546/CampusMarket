import { AuthLayout } from '@/layouts/AuthLayout';
import { LoginHero } from '../components/LoginHero';
import { LoginForm } from '../components/LoginForm';

export function LoginPage() {
  return (
    <AuthLayout hero={<LoginHero />}>
      <h2>Welcome back</h2>
      <p className="subtle" style={{ marginBottom: 10 }}>
        Log in to continue to CampusMarket.
      </p>
      <LoginForm />
    </AuthLayout>
  );
}
