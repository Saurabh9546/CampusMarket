# CampusMarket — Frontend

Auth feature + infrastructure layer for a college marketplace app. This covers identity
(register/login/verify/reset) — marketplace, selling, wishlist, and messaging build on
top of this once the backend's Product module exists.

## Structure

src/
api/ client.ts (fetch wrapper, token refresh) + auth.ts (typed endpoint calls)
context/ AuthContext + AuthProvider — single funnel for all auth operations
hooks/ useAuth
components/ Button, Input, FormError (each with a co-located .module.css)
_backlog/ Card, Checkbox, Divider, SocialButton — built, currently unused
features/
auth/ components (LoginForm, RegisterForm, LoginHero, AuthFooterLink),
pages, validation.ts, types.ts
marketplace/ HomePage and the rest of the marketplace UI
layouts/ AuthLayout (split hero/form shell)
routes/ AppRouter, ProtectedRoute
constants/ colors.ts (JS mirror of variables.css), routes.ts
styles/ globals.css (includes shared .subtle utility), variables.css
types/ shared domain types


## Auth flow

- Access token held in memory, refresh token in an httpOnly cookie. On a 401, `api/client.ts`
  attempts one silent refresh, then retries the original request once.
- `AuthProvider` doesn't run a session check unconditionally on mount — `ProtectedRoute`
  triggers `ensureSessionChecked()` itself, so logged-out visits to `/login` or `/register`
  don't pay for a doomed auth round trip.
- Register → email verification (real SMTP, one-time token) → login.
- Forgot password → email with a one-time reset token (1-hour expiry) → set new password.

## Run locally

```bash
npm install
npm run dev
```

Requires the Spring Boot backend running on `localhost:8080` — `vite.config.ts` proxies
`/api/*` there in dev.