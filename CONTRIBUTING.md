# Contributing

Thanks for contributing to Smart Expense Manager.

## Development workflow

1. Create a focused branch from `main`.
2. Make one logical change at a time.
3. Add or update tests for behavior changes.
4. Run the Maven test suite locally with `mvn test`.
5. Check that secrets and personal financial data are not committed.
6. Open a pull request describing the change, testing performed, and any migration or security implications.

## Code quality

Prefer small services, explicit validation, meaningful names, and predictable error handling. Changes affecting authentication, authorization, financial calculations, persistence, or AI behavior should include tests.

## Security

Never commit API keys, JWT secrets, database passwords, production credentials, or real user financial records. Report suspected vulnerabilities privately according to `SECURITY.md`.
