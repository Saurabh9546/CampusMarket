import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/layouts/AuthLayout';
import { Button } from '@/components/Button/Button';
import { useAuth } from '@/hooks/useAuth';
import { ROUTES } from '@/constants/routes';
import styles from './VerifyEmailPage.module.css';

type ViewState = 'pending' | 'verifying' | 'verified' | 'error' | 'expired';

/**
 * Handles both entry points:
 * 1. Right after registration (no token in URL) — "check your inbox" state.
 * 2. The user clicking the emailed verification link (?token=...) — calls
 *    GET /auth/verify and shows the result.
 */
export function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const emailFromRegistration = (location.state as { email?: string } | null)?.email;
  const { verifyEmail, resendVerification } = useAuth();

  const [view, setView] = useState<ViewState>(token ? 'verifying' : 'pending');
  const [resendCooldown, setResendCooldown] = useState(0);

  useEffect(() => {
    if (!token) return;
    (async () => {
      const result = await verifyEmail(token);
      if (result.success) {
        setView('verified');
      } else if (result.message.toLowerCase().includes('expired')) {
        setView('expired');
      } else {
        setView('error');
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [token]);

  useEffect(() => {
    if (resendCooldown <= 0) return;
    const timer = setInterval(() => setResendCooldown((s) => s - 1), 1000);
    return () => clearInterval(timer);
  }, [resendCooldown]);

  const handleResend = async () => {
    if (!emailFromRegistration || resendCooldown > 0) return;
    await resendVerification(emailFromRegistration);
    setResendCooldown(60); // basic client-side rate limit to prevent spam-clicking resend
  };

  if (view === 'verified') {
    return (
      <AuthLayout>
        <div className={styles.center}>
          <div className={styles.iconCircle}>✓</div>
          <h2>Email verified</h2>
          <p className="subtle">Your account is active. You can log in now.</p>
          <Button onClick={() => navigate(ROUTES.login)} style={{ marginTop: 16 }}>
            Continue to login
          </Button>
        </div>
      </AuthLayout>
    );
  }

  if (view === 'expired' || view === 'error') {
    return (
      <AuthLayout>
        <div className={styles.center}>
          <div className={`${styles.iconCircle} ${styles.errorCircle}`}>!</div>
          <h2>{view === 'expired' ? 'Link expired' : 'Verification failed'}</h2>
          <p className="subtle">
            {view === 'expired'
              ? 'This verification link has expired. Request a new one below.'
              : "We couldn't verify this link. It may have already been used."}
          </p>
          {emailFromRegistration && (
            <Button onClick={handleResend} disabled={resendCooldown > 0} style={{ marginTop: 16 }}>
              {resendCooldown > 0 ? `Resend in ${resendCooldown}s` : 'Resend email'}
            </Button>
          )}
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <div className={styles.center}>
        <div className={styles.iconCircle}>✉</div>
        <h2>Check your inbox</h2>
        <p className="subtle">
          {emailFromRegistration
            ? <>We sent a verification link to <b>{emailFromRegistration}</b>. Open it to activate your account.</>
            : 'We sent a verification link to your college email. Open it to activate your account.'}
        </p>
        <Button onClick={handleResend} disabled={resendCooldown > 0 || !emailFromRegistration} style={{ marginTop: 16 }}>
          {resendCooldown > 0 ? `Resend in ${resendCooldown}s` : 'Resend email'}
        </Button>
      </div>
    </AuthLayout>
  );
}