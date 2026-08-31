# Backlog components

Built but currently **zero consumers** anywhere in the app. Quarantined here instead
of sitting indistinguishable from load-bearing components in `components/`.

- **SocialButton** — Google/Microsoft/Apple login. No OAuth2 backend exists (auth is
  email+password only right now). Disabled by default, not rendered on any page. Move
  back to `components/` and wire it up only once OAuth2 login is actually built.
- **Card, Checkbox, Divider** — no current feature needs them. Keep here until one does,
  rather than guessing at props/variants ahead of an actual use case.

Moving something out of here: relocate the folder back to `src/components/`, update any
imports, and delete this note's line for that component.