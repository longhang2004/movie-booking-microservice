package com.example.booking_service.config;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class JwtDecoderFactory {

    private JwtDecoderFactory() {
    }

    static JwtDecoder create(String jwkSetUri, String publicKeyPem, String issuer, String audience) {
        NimbusJwtDecoder decoder;
        if (publicKeyPem != null && !publicKeyPem.isBlank()) {
            decoder = NimbusJwtDecoder.withPublicKey(parsePublicKey(publicKeyPem)).build();
        } else if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            throw new IllegalStateException("Configure app.jwt.jwk-set-uri or app.jwt.public-key");
        }
        decoder.setJwtValidator(validators(issuer, audience));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> validators(String issuer, String audience) {
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt -> {
            if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Invalid audience", null));
        };
        return new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
    }

    static RSAPublicKey parsePublicKey(String pem) {
        try {
            String stripped = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(stripped);
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid RSA public key", ex);
        }
    }
}
