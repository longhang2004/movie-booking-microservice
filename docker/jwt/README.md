Lab RSA-2048 keypair for **local Compose only**. Auth loads these via
`JWT_PRIVATE_KEY_LOCATION` / `JWT_PUBLIC_KEY_LOCATION` so access tokens
survive auth-service restarts (`kid=auth-service-rsa`).

Rotate with `FORCE=1 bash scripts/generate-jwt-keys.sh`. Do not reuse these
files outside this repository. They are committed so the learning lab boots
without extra steps — not because they are production secrets.

See [docs/architecture.md](../../docs/architecture.md#jwt-and-jwks).
