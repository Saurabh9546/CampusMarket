import { AuthLayout } from '@/layouts/AuthLayout';
import { RegisterForm } from '../components/RegisterForm';

export function RegisterPage() {
  return (
    <AuthLayout>
      <h2>Create your account</h2>
      <p className="subtle" style={{ marginBottom: 10 }}>
        Use your college email — that's how we verify you.
      </p>
      <RegisterForm />
    </AuthLayout>
  );
}
