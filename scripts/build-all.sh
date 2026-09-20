#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
echo "=== Installing platform-security ==="
"$ROOT/auth-service/mvnw" -f "$ROOT/platform-security/pom.xml" -q clean install -DskipTests
for service in discovery-server api-gateway auth-service movie-service theater-service showtime-service booking-service payment-service; do
  echo "=== Building $service ==="
  (cd "$ROOT/$service" && ./mvnw -q clean package -DskipTests)
done
echo "All services packaged."
