package com.example.platform.security.jwt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemKeys {

    private PemKeys() {
    }

    public static String material(String pem, String location) {
        if (pem != null && !pem.isBlank()) {
            return pem;
        }
        if (location == null || location.isBlank()) {
            return "";
        }
        Path path = Path.of(location.startsWith("file:") ? location.substring("file:".length()) : location);
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to read key from " + location, ex);
        }
    }

    public static RSAPublicKey publicKey(String pem) {
        try {
            byte[] der = decodePem(pem, "PUBLIC KEY");
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid RSA public key", ex);
        }
    }

    public static RSAPrivateKey privateKey(String pem) {
        try {
            byte[] der = decodePem(pem, "PRIVATE KEY");
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid RSA private key", ex);
        }
    }

    private static byte[] decodePem(String pem, String type) {
        if (pem == null) {
            throw new IllegalArgumentException("PEM is required");
        }
        String stripped = pem.replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(stripped);
    }
}
