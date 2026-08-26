# Security

Do not commit signing keys, passwords, API keys, tokens, or other secrets.

The production signing key used for existing installations must remain outside the public repository. For CI, use GitHub Actions encrypted secrets or a dedicated secret-management solution.
