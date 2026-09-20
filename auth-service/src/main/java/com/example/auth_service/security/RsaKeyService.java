package com.example.auth_service.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

@Component
public class RsaKeyService {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyService.class);

    private final RSAKey rsaKey;

    public RsaKeyService(
            @Value("${app.jwt.private-key:}") String privatePem,
            @Value("${app.jwt.public-key:}") String publicPem) {
        try {
            this.rsaKey = loadOrGenerate(privatePem, publicPem);
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

    private RSAKey loadOrGenerate(String privatePem, String publicPem) throws Exception {
        String kid = "auth-service-rsa";
        if (privatePem != null && !privatePem.isBlank() && publicPem != null && !publicPem.isBlank()) {
            RSAPrivateKey privateKey = parsePrivate(privatePem);
            RSAPublicKey publicKey = parsePublic(publicPem);
            return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(kid).build();
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        return new RSAKey.Builder((RSAPublicKey) pair.getPublic())
                .privateKey((RSAPrivateKey) pair.getPrivate())
                .keyID(kid + "-" + UUID.randomUUID().toString().substring(0, 8))
                .build();
    }

    private RSAPublicKey parsePublic(String pem) throws Exception {
        byte[] der = decodePem(pem, "PUBLIC KEY");
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private RSAPrivateKey parsePrivate(String pem) throws Exception {
        byte[] der = decodePem(pem, "PRIVATE KEY");
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private byte[] decodePem(String pem, String type) {
        String stripped = pem.replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(stripped);
    }
}
