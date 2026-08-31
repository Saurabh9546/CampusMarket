import { apiClient } from './client';
import type { ApiResponse, User } from '@/types';

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface LoginResult {
  accessToken: string;
  user: User;
}

/** POST /auth/register — creates an unverified account and triggers a verification email. */
export function register(payload: RegisterPayload): Promise<ApiResponse<{ userId: string }>> {
  return apiClient.post('/auth/register', payload);
}

/** POST /auth/login — returns a JWT access token; refresh token arrives as an httpOnly cookie set by the server. */
export function login(payload: LoginPayload): Promise<ApiResponse<LoginResult>> {
  return apiClient.post('/auth/login', payload);
}

/** GET /auth/verify?token=... */
export function verifyEmail(token: string): Promise<ApiResponse<null>> {
  return apiClient.get(`/auth/verify?token=${encodeURIComponent(token)}`);
}

/**
 * Backend endpoint for this isn't implemented yet — stubbed here so the
 * frontend compiles against the intended contract. Don't wire this up until
 * the backend actually supports it.
 */
export function resendVerification(email: string): Promise<ApiResponse<null>> {
  return apiClient.post('/auth/resend-verification', { email });
}

export function logout(): Promise<ApiResponse<null>> {
  return apiClient.post('/auth/logout');
}

export function fetchCurrentUser(): Promise<ApiResponse<User>> {
  return apiClient.get('/users/me');
}

/** POST /auth/forgot-password — always succeeds silently, even for unknown emails. */
export function forgotPassword(email: string): Promise<ApiResponse<null>> {
  return apiClient.post('/auth/forgot-password', { email });
}

/** POST /auth/reset-password — token is single-use, expires in 1 hour. */
export function resetPassword(token: string, newPassword: string): Promise<ApiResponse<null>> {
  return apiClient.post('/auth/reset-password', { token, newPassword });
}