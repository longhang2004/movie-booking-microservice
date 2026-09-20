package com.example.platform.security.jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtDecoderFactoryTest {

    private static RSAPublicKey publicKey;
    private static RSAPrivateKey privateKey;
    private static String publicPem;

    @BeforeAll
    static void generateKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        publicKey = (RSAPublicKey) pair.getPublic();
        privateKey = (RSAPrivateKey) pair.getPrivate();
        publicPem = PemKeysTest.toPublicPem(publicKey);
    }

    @Test
    void decodesRs256TokenWhenPublicKeyConfigured() throws Exception {
        JwtDecoder decoder = JwtDecoderFactory.create("", publicPem, "movie-booking", "movie-booking-api");
        String token = signedToken("movie-booking", List.of("movie-booking-api"));

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getClaimAsString("iss")).isEqualTo("movie-booking");
        assertThat(jwt.getAudience()).contains("movie-booking-api");
        assertThat(jwt.getSubject()).isEqualTo("user@cinema.local");
    }

    @Test
    void rejectsTokenWithWrongAudience() throws Exception {
        JwtDecoder decoder = JwtDecoderFactory.create("", publicPem, "movie-booking", "movie-booking-api");
        String token = signedToken("movie-booking", List.of("other-api"));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    @Test
    void prefersInlinePublicKeyOverJwkSetUri() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setJwkSetUri("http://localhost:1/does-not-exist");
        properties.setPublicKey(publicPem);
        properties.setIssuer("movie-booking");
        properties.setAudience("movie-booking-api");

        JwtDecoder decoder = JwtDecoderFactory.create(properties);
        Jwt jwt = decoder.decode(signedToken("movie-booking", List.of("movie-booking-api")));

        assertThat(jwt.getSubject()).isEqualTo("user@cinema.local");
    }

    @Test
    void requiresJwkSetUriOrPublicKey() {
        assertThatThrownBy(() -> JwtDecoderFactory.create("", "", "movie-booking", "movie-booking-api"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.jwk-set-uri or app.jwt.public-key");
    }

    private static String signedToken(String issuer, List<String> audience) throws Exception {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("user@cinema.local")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(60)))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner(privateKey));
        return jwt.serialize();
    }
}
