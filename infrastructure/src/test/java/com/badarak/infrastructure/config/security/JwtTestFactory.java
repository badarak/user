package com.badarak.infrastructure.config.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.time.temporal.ChronoUnit.HOURS;

public final class JwtTestFactory {
    public static final String ISSUER = "https://auth.test.badarak";
    public static final String AUDIENCE = "user-api";

    private static final KeyPair KEY_PAIR = generateRsaKeyPair();
    private static final JwtEncoder ENCODER = encoder(KEY_PAIR);

    private JwtTestFactory() {
    }

    /** Public key in the format expected by {@code security.jwt.public-key}: base64 of the PEM. */
    public static String publicKeyBase64() {
        final var pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(US_ASCII)).encodeToString(KEY_PAIR.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";
        return Base64.getEncoder().encodeToString(pem.getBytes(US_ASCII));
    }

    public static String validToken() {
        return token(claims -> {
        });
    }

    public static String token(Consumer<JwtClaimsSet.Builder> claimsCustomizer) {
        return sign(ENCODER, claimsCustomizer);
    }

    public static String tokenSignedWithUnknownKey() {
        return sign(encoder(generateRsaKeyPair()), claims -> {
        });
    }

    public static String unsignedToken() {
        final var base64Url = Base64.getUrlEncoder().withoutPadding();
        final var header = """
                {"alg":"none"}""";
        final var payload = """
                {"iss":"%s","aud":["%s"],"sub":"test-user","exp":%d}"""
                .formatted(ISSUER, AUDIENCE, Instant.now().plus(1, HOURS).getEpochSecond());
        return base64Url.encodeToString(header.getBytes(US_ASCII)) + "."
                + base64Url.encodeToString(payload.getBytes(US_ASCII)) + ".";
    }

    private static String sign(JwtEncoder encoder, Consumer<JwtClaimsSet.Builder> claimsCustomizer) {
        final var now = Instant.now();
        final var claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .subject("test-user")
                .claim("scope", "users:read users:write")
                .issuedAt(now)
                .expiresAt(now.plus(1, HOURS));
        claimsCustomizer.accept(claims);
        final var header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    private static KeyPair generateRsaKeyPair() {
        try {
            final var generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JwtEncoder encoder(KeyPair keyPair) {
        final var jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .privateKey((RSAPrivateKey) keyPair.getPrivate())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }
}
