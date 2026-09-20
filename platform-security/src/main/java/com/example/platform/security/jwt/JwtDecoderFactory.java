package com.example.platform.security.jwt;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

public final class JwtDecoderFactory {

    private JwtDecoderFactory() {
    }

    public static JwtDecoder create(JwtProperties properties) {
        String publicPem = PemKeys.material(properties.getPublicKey(), properties.getPublicKeyLocation());
        return create(properties.getJwkSetUri(), publicPem, properties.getIssuer(), properties.getAudience());
    }

    public static JwtDecoder create(String jwkSetUri, String publicKeyPem, String issuer, String audience) {
        NimbusJwtDecoder decoder;
        if (publicKeyPem != null && !publicKeyPem.isBlank()) {
            decoder = NimbusJwtDecoder.withPublicKey(PemKeys.publicKey(publicKeyPem)).build();
        } else if (jwkSetUri != null && !jwkSetUri.isBlank()) {
            decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        } else {
            throw new IllegalStateException("Configure app.jwt.jwk-set-uri or app.jwt.public-key");
        }
        decoder.setJwtValidator(JwtClaimValidators.issuerAndAudience(issuer, audience));
        return decoder;
    }
}
