import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/layouts/AuthLayout';
import { AuthHero } from '../components/AuthHero';
import { Button } from '@/components/Button/Button';
import { useAuth } from '@/hooks/useAuth';
import { ROUTES } from '@/constants/routes';
import styles from './VerifyEmailPage.module.css';

type ViewState = 'pending' | 'confirm' | 'verifying' | 'verified' | 'error' | 'expired';

/**
 * Handles both entry points:
 * 1. Right after registration (no token in URL) — "check your inbox" state.
 * 2. The user clicking the emailed verification link (?token=...) — shows a
 *    confirm button before calling GET /auth/verify. Verification is NOT
 *    triggered automatically on page load: many email clients and in-app
 *    browsers (Outlook Safe Links, Gmail scanners, Instagram/Snapchat's
 *    in-app browser) prefetch links before the user taps them, which would
 *    silently consume a one-time token if the API call fired on mount.
 *    Requiring an explicit click means only a real user action verifies.
 */
export function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const emailFromRegistration = (location.state as { email?: string } | null)?.email;
  const { verifyEmail, resendVerification } = useAuth();

  const [view, setView] = useState<ViewState>(token ? 'confirm' : 'pending');
  const [resendCooldown, setResendCooldown] = useState(0);

  const handleConfirmVerify = async () => {
    if (!token) return;
    setView('verifying');
    const result = await verifyEmail(token);
    if (result.success) {
      setView('verified');
    } else if (result.message.toLowerCase().includes('expired')) {
      setView('expired');
    } else {
      setView('error');
    }
  };

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

  if (view === 'confirm') {
    return (
      <AuthLayout hero={<AuthHero />} stacked>
        <div className={styles.center}>
          <div className={styles.iconCircle}>✉</div>
          <h2>Confirm your email</h2>
          <p className="subtle">Tap below to verify your account.</p>
          <Button onClick={() => void handleConfirmVerify()} style={{ marginTop: 16 }}>
            Verify my email
          </Button>
        </div>
      </AuthLayout>
    );
  }

  if (view === 'verifying') {
    return (
      <AuthLayout hero={<AuthHero />} stacked>
        <div className={styles.center}>
          <h2>Verifying…</h2>
        </div>
      </AuthLayout>
    );
  }

  if (view === 'verified') {
    return (
      <AuthLayout hero={<AuthHero />} stacked>
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
      <AuthLayout hero={<AuthHero />} stacked>
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
    <AuthLayout hero={<AuthHero />} stacked>
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