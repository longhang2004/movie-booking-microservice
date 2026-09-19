.PHONY: test package up down smoke
test:
	@for s in discovery-server api-gateway auth-service movie-service theater-service showtime-service booking-service payment-service; do \
	  echo "== $$s =="; (cd $$s && ./mvnw -q test); \
	done

package:
	bash scripts/build-all.sh

up: package
	docker compose up --build -d

down:
	docker compose down -v

smoke:
	bash scripts/smoke-test.sh
