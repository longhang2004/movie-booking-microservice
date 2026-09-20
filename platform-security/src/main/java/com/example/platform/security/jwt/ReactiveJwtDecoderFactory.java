package com.example.platform.security.jwt;

import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

public final class ReactiveJwtDecoderFactory {

    private ReactiveJwtDecoderFactory() {
    }

    public static ReactiveJwtDecoder create(JwtProperties properties) {
        String publicPem = PemKeys.material(properties.getPublicKey(), properties.getPublicKeyLocation());
        return create(properties.getJwkSetUri(), publicPem, properties.getIssuer(), properties.getAudience());
    }

    public static ReactiveJwtDecoder create(String jwkSetUri, String publicKeyPem, String issuer, String audience) {
        NimbusReactiveJwtDecoder decoder;
        if (publicKeyPem != null && !publicKeyPem.isBlank()) {
            decoder = NimbusReactiveJwtDecoder.withPublicKey(PemKeys.publicKey(publicKeyPem)).build();
        } else if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            throw new IllegalStateException("Configure app.jwt.jwk-set-uri or app.jwt.public-key");
        }
        decoder.setJwtValidator(JwtClaimValidators.issuerAndAudience(issuer, audience));
        return decoder;
    }
}
