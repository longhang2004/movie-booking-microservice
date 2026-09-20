package com.example.platform.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtClaimValidatorsTest {

    @Test
    void acceptsTokenWithMatchingIssuerAndAudience() {
        OAuth2TokenValidator<Jwt> validator = JwtClaimValidators.issuerAndAudience("movie-booking", "movie-booking-api");
        Jwt jwt = jwt("movie-booking", List.of("movie-booking-api"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isFalse();
    }

    @Test
    void rejectsTokenWithWrongAudience() {
        OAuth2TokenValidator<Jwt> validator = JwtClaimValidators.issuerAndAudience("movie-booking", "movie-booking-api");
        Jwt jwt = jwt("movie-booking", List.of("someone-else"));

        OAuth2TokenValidatorResult result = validator.validate(jwt);

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors())
                .anyMatch(error -> "invalid_token".equals(error.getErrorCode())
                        && error.getDescription().contains("Invalid audience"));
    }

    private static Jwt jwt(String issuer, List<String> audience) {
        Instant now = Instant.now();
        return new Jwt(
                "token",
                now,
                now.plusSeconds(60),
                Map.of("alg", "RS256"),
                Map.of("iss", issuer, "aud", audience, "sub", "user@cinema.local"));
    }
}
