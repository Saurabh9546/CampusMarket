import type { ApiResponse } from '@/types';

/**
 * Thin fetch wrapper handling authenticated requests:
 * - access token held in memory (via getAccessToken/setAccessToken, wired by AuthProvider)
 * - refresh token lives in an httpOnly cookie the browser sends automatically
 *   (credentials: 'include') — this file never reads or writes that cookie directly
 * - on a 401, attempt exactly one silent refresh, then retry the original request once
 *
 * Network-failure handling: fetch() itself can reject (server unreachable, DNS
 * failure, connection refused) rather than resolving with a bad status code.
 * That's a different failure mode from a 401/403/500 — the server never got a
 * chance to respond at all. Without catching it here, every caller across the
 * app (loadProducts, ensureSessionChecked, wishlist list, etc.) would need its
 * own try/catch or risk an unhandled rejection and a component stuck mid-loading
 * forever. Catching it here means ALL callers get the same ApiResponse shape
 * regardless of failure type, and can distinguish network failure from a
 * real auth rejection via the `networkError` flag if they need to.
 */

const BASE_URL = '/api/v1';

let accessToken: string | null = null;
let onAuthExpired: (() => void) | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

export function getAccessToken() {
  return accessToken;
}

/** Wired by AuthProvider so this module can clear session state on an unrecoverable 401. */
export function setAuthExpiredHandler(handler: () => void) {
  onAuthExpired = handler;
}

interface RequestOptions extends RequestInit {
  skipAuthRetry?: boolean;
}

async function rawRequest<T>(path: string, options: RequestOptions = {}): Promise<ApiResponse<T>> {
  const headers = new Headers(options.headers);
  headers.set('Content-Type', 'application/json');
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);

  let response: Response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      ...options,
      headers,
      credentials: 'include', // sends the httpOnly refresh-token cookie
    });
  } catch {
    // Server unreachable — not a 401, not an auth failure. Do NOT trigger
    // onAuthExpired here; that's reserved for a real rejected session.
    return {
      success: false,
      message: 'Could not reach the server. Check your connection and try again.',
      networkError: true,
    };
  }

  if (response.status === 401 && !options.skipAuthRetry) {
    const refreshed = await tryRefresh();
    if (refreshed) {
      return rawRequest<T>(path, { ...options, skipAuthRetry: true });
    }
    onAuthExpired?.();
  }

  const body = (await response.json().catch(() => null)) as ApiResponse<T> | null;
  if (!body) {
    return { success: false, message: `Unexpected response (HTTP ${response.status})` };
  }
  return body;
}

let refreshInFlight: Promise<boolean> | null = null;

async function tryRefresh(): Promise<boolean> {
  // De-dupe concurrent refresh attempts if multiple requests 401 at once.
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      try {
        const res = await fetch(`${BASE_URL}/auth/refresh`, {
          method: 'POST',
          credentials: 'include',
        });
        if (!res.ok) return false;
        const body = (await res.json()) as ApiResponse<{ accessToken: string }>;
        if (body.success) {
          setAccessToken(body.data.accessToken);
          return true;
        }
        return false;
      } catch {
        return false;
      } finally {
        refreshInFlight = null;
      }
    })();
  }
  return refreshInFlight;
}

export const apiClient = {
  get: <T>(path: string) => rawRequest<T>(path, { method: 'GET' }),
  post: <T>(path: string, body?: unknown) =>
    rawRequest<T>(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  put: <T>(path: string, body?: unknown) =>
    rawRequest<T>(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(path: string, body?: unknown) =>
    rawRequest<T>(path, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(path: string) => rawRequest<T>(path, { method: 'DELETE' }),
};