# Development Guide

## Backend

The backend uses Java 17, Spring Boot, Spring Security, JPA/Hibernate, and PostgreSQL.

### Configuration

Keep local and production secrets outside source control. Configure database credentials, JWT signing material, and external AI credentials through environment variables.

### Verify changes

From the `backend` directory:

```bash
mvn test
```

## Frontend

The frontend is built with HTML/CSS/JavaScript, Tailwind CSS, and Chart.js. Keep API configuration separate from secrets and never expose server-side credentials in browser code.

## Contribution checklist

Before opening a pull request:

- Run the backend test suite.
- Verify authentication and authorization behavior.
- Check that sensitive values are not present in logs or committed configuration.
- Test affected API endpoints and UI flows.
- Update documentation when behavior or configuration changes.
