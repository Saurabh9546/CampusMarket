import { createContext, useCallback, useMemo, useRef, useState, type ReactNode } from 'react';
import type { ApiResponse, User } from '@/types';
import { setAccessToken, setAuthExpiredHandler } from '@/api/client';
import * as authApi from '@/api/auth';
import type { LoginResult } from '@/api/auth';

/**
 * Single funnel for all auth operations. Every features/auth page calls
 * these methods — none call `api/auth.ts` directly. This replaces three
 * different access patterns that previously existed (Login via context,
 * Register and VerifyEmail hitting the API layer directly).
 *
 * Return type is `ApiResponse<T>` throughout — the same envelope api/client.ts
 * already returns — rather than a second, ad-hoc {success, message} shape.
 * Consumers check `result.success` and read `result.data` or `result.message`.
 */
interface AuthContextValue {
  currentUser: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<ApiResponse<LoginResult>>;
  register: (name: string, email: string, password: string) => Promise<ApiResponse<{ userId: string }>>;
  verifyEmail: (token: string) => Promise<ApiResponse<null>>;
  resendVerification: (email: string) => Promise<ApiResponse<null>>;
  logout: () => Promise<void>;
  /** Called once by ProtectedRoute on first mount — never called from public pages. */
  ensureSessionChecked: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const sessionCheckRef = useRef<Promise<void> | null>(null);

  const clearSession = useCallback(() => {
    setAccessToken(null);
    setCurrentUser(null);
  }, []);

  useMemo(() => {
    setAuthExpiredHandler(clearSession);
  }, [clearSession]);

  /**
   * Gated session check (fix for the "doomed request on every page load"
   * perf issue): only runs when a protected route actually mounts and
   * requests it, not unconditionally for every visitor including logged-out
   * ones on /login or /register. De-duped via sessionCheckRef so concurrent
   * ProtectedRoute mounts don't fire it twice.
   */
  const ensureSessionChecked = useCallback((): Promise<void> => {
    if (currentUser) return Promise.resolve(); // already known from a just-completed login
    if (!sessionCheckRef.current) {
      sessionCheckRef.current = (async () => {
        const result = await authApi.fetchCurrentUser();
        if (result.success) setCurrentUser(result.data);
        setIsLoading(false);
      })();
    }
    return sessionCheckRef.current;
  }, [currentUser]);

  const login = useCallback(async (email: string, password: string) => {
    const result = await authApi.login({ email, password });
    if (result.success) {
      setAccessToken(result.data.accessToken);
      setCurrentUser(result.data.user);
      setIsLoading(false);
    }
    return result;
  }, []);

  const register = useCallback(async (name: string, email: string, password: string) => {
    return authApi.register({ name, email, password });
  }, []);

  const verifyEmail = useCallback(async (token: string) => {
    return authApi.verifyEmail(token);
  }, []);

  const resendVerification = useCallback(async (email: string) => {
    return authApi.resendVerification(email);
  }, []);

  const logout = useCallback(async () => {
    await authApi.logout();
    clearSession();
  }, [clearSession]);

  const value = useMemo<AuthContextValue>(
    () => ({
      currentUser,
      isAuthenticated: currentUser != null,
      isLoading,
      login,
      register,
      verifyEmail,
      resendVerification,
      logout,
      ensureSessionChecked,
    }),
    [currentUser, isLoading, login, register, verifyEmail, resendVerification, logout, ensureSessionChecked],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
