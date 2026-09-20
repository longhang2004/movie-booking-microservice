package com.example.auth_service.security;

import com.example.platform.security.jwt.JwtProperties;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyServiceTest {

    @Test
    void generatesEphemeralKidWhenPemMissing() {
        RsaKeyService service = new RsaKeyService(new JwtProperties());

        assertThat(service.rsaKey().getKeyID()).startsWith(RsaKeyService.STABLE_KID + "-");
        assertThat(service.publicJwkSet().getKeys()).hasSize(1);
    }

    @Test
    void loadsStableKidFromInlinePem() throws Exception {
        KeyPair pair = rsaPair();
        JwtProperties properties = new JwtProperties();
        properties.setPrivateKey(toPem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        properties.setPublicKey(toPem("PUBLIC KEY", pair.getPublic().getEncoded()));

        RsaKeyService service = new RsaKeyService(properties);

        assertThat(service.rsaKey().getKeyID()).isEqualTo(RsaKeyService.STABLE_KID);
        assertThat(service.rsaKey().toRSAPublicKey().getModulus())
                .isEqualTo(((RSAPublicKey) pair.getPublic()).getModulus());
        assertThat(service.rsaKey().toRSAPrivateKey().getModulus())
                .isEqualTo(((RSAPrivateKey) pair.getPrivate()).getModulus());
    }

    @Test
    void loadsFromFileLocations(@TempDir Path tempDir) throws Exception {
        KeyPair pair = rsaPair();
        Path privateFile = tempDir.resolve("private.pem");
        Path publicFile = tempDir.resolve("public.pem");
        Files.writeString(privateFile, toPem("PRIVATE KEY", pair.getPrivate().getEncoded()));
        Files.writeString(publicFile, toPem("PUBLIC KEY", pair.getPublic().getEncoded()));

        JwtProperties properties = new JwtProperties();
        properties.setPrivateKeyLocation(privateFile.toString());
        properties.setPublicKeyLocation("file:" + publicFile);

        RSAKey key = new RsaKeyService(properties).rsaKey();

        assertThat(key.getKeyID()).isEqualTo(RsaKeyService.STABLE_KID);
        assertThat(key.toRSAPublicKey().getModulus()).isEqualTo(((RSAPublicKey) pair.getPublic()).getModulus());
    }

    private static KeyPair rsaPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static String toPem(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }
}
