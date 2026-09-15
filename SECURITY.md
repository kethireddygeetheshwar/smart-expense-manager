# Security Policy

## Reporting a vulnerability

Please report suspected security issues privately to the repository owner. Do not publish credentials, exploit details, or sensitive user data in a public issue.

## Security baseline

- Database credentials and AI provider credentials are loaded from environment variables.
- JWT signing configuration is externalized from source-controlled application configuration.
- Authentication and authorization are enforced by Spring Security.
- Input validation should be applied at API boundaries before persistence or external AI calls.
- Production deployments should use HTTPS, managed secrets, least-privilege database credentials, and restricted network access.

## Production checklist

- Rotate any credential that has ever been committed to source control.
- Use a strong, randomly generated JWT signing secret stored in a secret manager.
- Use a dedicated database account with only the permissions required by the application.
- Restrict CORS to the exact production frontend origins.
- Add rate limiting to authentication and AI-backed endpoints.
- Avoid logging passwords, tokens, authorization headers, raw financial records, or AI provider secrets.
- Review authorization on every resource access so one user cannot access another user's expenses, budgets, goals, chats, or reports.
- Keep dependencies patched and run automated dependency/security checks in CI.
