#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIR="$ROOT/docker/jwt"
mkdir -p "$DIR"
if [[ -f "$DIR/private.pem" && -f "$DIR/public.pem" && "${FORCE:-}" != "1" ]]; then
  echo "JWT keys already exist in $DIR (set FORCE=1 to rotate)"
  exit 0
fi
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$DIR/private.pem"
openssl rsa -pubout -in "$DIR/private.pem" -out "$DIR/public.pem"
chmod 600 "$DIR/private.pem"
echo "Wrote $DIR/private.pem and $DIR/public.pem (lab keys, not for production)"
