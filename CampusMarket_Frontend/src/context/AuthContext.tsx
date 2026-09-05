import { createContext, useCallback, useMemo, useRef, useState, type ReactNode } from 'react';
import type { ApiResponse, User } from '@/types';
import { setAccessToken, setAuthExpiredHandler } from '@/api/client';
import * as authApi from '@/api/auth';
import * as usersApi from '@/api/users';
import type { LoginResult } from '@/api/auth';

/**
 * All auth calls go through this context instead of hitting api/auth.ts
 * directly, so there's one place handling login/register/verify/etc.
 *
 * Everything returns the same ApiResponse<T> shape as api/client.ts —
 * check result.success, then read result.data or result.message.
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
  updateProfile: (name: string) => Promise<ApiResponse<User>>;
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
   * Only checks the session when a protected page actually needs it —
   * not on every page load. sessionCheckRef stops it from firing twice
   * if multiple protected routes mount at once.
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

  const updateProfile = useCallback(async (name: string) => {
    const result = await usersApi.updateProfile({ name });
    if (result.success) {
      setCurrentUser(result.data);
    }
    return result;
  }, []);

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
      updateProfile,
      ensureSessionChecked,
    }),
    [currentUser, isLoading, login, register, verifyEmail, resendVerification, logout, updateProfile, ensureSessionChecked],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}