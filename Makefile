.PHONY: test package up down smoke load keys
MVNW=auth-service/mvnw

keys:
	bash scripts/generate-jwt-keys.sh

test:
	$(MVNW) -f platform-security/pom.xml -q clean install
	@for s in discovery-server api-gateway auth-service movie-service theater-service showtime-service booking-service payment-service; do \
	  echo "== $$s =="; (cd $$s && ./mvnw -q test); \
	done

package:
	bash scripts/build-all.sh

up: keys package
	docker compose up --build -d

down:
	docker compose down -v

smoke:
	bash scripts/smoke-test.sh

load:
	@command -v k6 >/dev/null || { echo "k6 is required: https://grafana.com/docs/k6/latest/set-up/install-k6/"; exit 1; }
	k6 run scripts/load/booking.js
