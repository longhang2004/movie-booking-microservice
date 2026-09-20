package com.example.auth_service.security;

import com.example.platform.security.jwt.JwtProperties;
import com.example.platform.security.jwt.PemKeys;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Component
public class RsaKeyService {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyService.class);
    static final String STABLE_KID = "auth-service-rsa";

    private final RSAKey rsaKey;

    public RsaKeyService(JwtProperties jwtProperties) {
        try {
            this.rsaKey = loadOrGenerate(jwtProperties);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize RSA signing key", ex);
        }
        log.info("JWT signing key kid={}", rsaKey.getKeyID());
    }

    public RSAKey rsaKey() {
        return rsaKey;
    }

    public JWKSet publicJwkSet() {
        return new JWKSet(rsaKey.toPublicJWK());
    }

    private RSAKey loadOrGenerate(JwtProperties properties) throws Exception {
        String privatePem = PemKeys.material(properties.getPrivateKey(), properties.getPrivateKeyLocation());
        String publicPem = PemKeys.material(properties.getPublicKey(), properties.getPublicKeyLocation());
        if (!privatePem.isBlank() && !publicPem.isBlank()) {
            RSAPrivateKey privateKey = PemKeys.privateKey(privatePem);
            RSAPublicKey publicKey = PemKeys.publicKey(publicPem);
            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(STABLE_KID).build();
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate())
                .keyID(STABLE_KID + "-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
    }
}
