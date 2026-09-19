#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8090}"

echo "== Health =="
curl -sf "$BASE/actuator/health" | head -c 200; echo

echo "== Login =="
LOGIN=$(curl -sf -X POST "$BASE/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@cinema.local","password":"User@123"}')
TOKEN=$(python3 - << PY
import json,sys,os
print(json.loads('''$LOGIN''')["accessToken"])
PY
)
echo "token acquired"

echo "== Catalog =="
curl -sf "$BASE/api/v1/movies?size=5" | python3 -m json.tool | head
curl -sf "$BASE/api/v1/showtimes" | python3 -m json.tool | head

echo "== Booking =="
BOOKING=$(curl -sf -X POST "$BASE/api/v1/bookings" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: smoke-$(date +%s)" \
  -H 'Content-Type: application/json' \
  -d '{"showtimeId":1,"seats":["A1","A2"]}')
echo "$BOOKING" | python3 -m json.tool
BOOKING_ID=$(python3 - << PY
import json
print(json.loads('''$BOOKING''')["id"])
PY
)

echo "== Wait for async payment =="
sleep 4
curl -sf -H "Authorization: Bearer $TOKEN" "$BASE/api/v1/bookings/$BOOKING_ID" | python3 -m json.tool
curl -sf -H "Authorization: Bearer $TOKEN" "$BASE/api/v1/payments/$BOOKING_ID" | python3 -m json.tool

echo "Smoke test finished."
