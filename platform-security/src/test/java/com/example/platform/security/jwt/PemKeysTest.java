package com.example.platform.security.jwt;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PemKeysTest {

    @Test
    void publicAndPrivateKeyRoundTripFromPem() throws Exception {
        KeyPair pair = rsaPair();
        RSAPublicKey parsedPublic = PemKeys.publicKey(toPublicPem((RSAPublicKey) pair.getPublic()));
        RSAPrivateKey parsedPrivate = PemKeys.privateKey(toPrivatePem((RSAPrivateKey) pair.getPrivate()));

        assertThat(parsedPublic.getModulus()).isEqualTo(((RSAPublicKey) pair.getPublic()).getModulus());
        assertThat(parsedPrivate.getModulus()).isEqualTo(((RSAPrivateKey) pair.getPrivate()).getModulus());
    }

    @Test
    void materialPrefersInlinePemOverFileLocation() {
        String inline = "-----BEGIN PUBLIC KEY-----\nabc\n-----END PUBLIC KEY-----";
        assertThat(PemKeys.material(inline, "/does-not-exist.pem")).isEqualTo(inline);
    }

    @Test
    void materialReadsFromFileWhenPemBlank(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("public.pem");
        Files.writeString(file, "from-disk");
        assertThat(PemKeys.material("  ", file.toString())).isEqualTo("from-disk");
        assertThat(PemKeys.material("", "file:" + file)).isEqualTo("from-disk");
    }

    @Test
    void materialReturnsEmptyWhenNeitherPemNorLocationProvided() {
        assertThat(PemKeys.material("", "")).isEmpty();
        assertThat(PemKeys.material(null, null)).isEmpty();
    }

    @Test
    void publicKeyRejectsGarbage() {
        assertThatThrownBy(() -> PemKeys.publicKey("not-a-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid RSA public key");
    }

    private static KeyPair rsaPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    static String toPublicPem(RSAPublicKey key) {
        return wrap("PUBLIC KEY", key.getEncoded());
    }

    static String toPrivatePem(RSAPrivateKey key) {
        return wrap("PRIVATE KEY", key.getEncoded());
    }

    private static String wrap(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }
}
