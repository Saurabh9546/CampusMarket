import { useEffect, type ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import { ROUTES } from '@/constants/routes';

interface ProtectedRouteProps {
  children: ReactNode;
}

/**
 * Triggers ensureSessionChecked() itself — AuthProvider no longer runs this
 * unconditionally on app mount. Fixes the perf issue where every visitor,
 * including logged-out ones on /login or /register, paid for a doomed
 * GET /users/me → 401 → failed refresh cycle before the page was interactive.
 * Now that round-trip only happens for routes that actually require a session.
 */
export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, ensureSessionChecked } = useAuth();

  useEffect(() => {
    void ensureSessionChecked();
  }, [ensureSessionChecked]);

  if (isLoading) {
    // Session check triggered above is still in flight — avoid a flash-redirect
    // to /login before we know if the refresh cookie is valid.
    return null;
  }

  if (!isAuthenticated) {
    return <Navigate to={ROUTES.login} replace />;
  }

  return <>{children}</>;
}
