#!/usr/bin/env bash
set -euo pipefail
# Retries Maven for flaky distribution / Central fetches in CI.
n=0
until mvn "$@"; do
  n=$((n + 1))
  if [[ "$n" -ge 4 ]]; then
    exit 1
  fi
  echo "Maven failed (attempt ${n}), retrying in $((n * 5))s..." >&2
  sleep $((n * 5))
done
