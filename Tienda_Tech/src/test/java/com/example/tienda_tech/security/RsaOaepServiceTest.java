package com.example.tienda_tech.security;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RsaOaepServiceTest {
    @Test
    void wrapsAesKeyWithRandomizedOaepAndRecoversIt() {
        RsaOaepService rsa = new RsaOaepService("", "");
        byte[] aesKey = new byte[32];
        new SecureRandom().nextBytes(aesKey);

        String first = rsa.wrap(aesKey);
        String second = rsa.wrap(aesKey);

        assertNotEquals(first, second);
        assertArrayEquals(aesKey, rsa.unwrap(first));
        assertArrayEquals(aesKey, rsa.unwrap(second));
    }

    @Test
    void loadsPersistentPkcs8AndX509Keys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        RsaOaepService rsa = new RsaOaepService(
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()));

        byte[] aesKey = new byte[32];
        new SecureRandom().nextBytes(aesKey);
        assertArrayEquals(aesKey, rsa.unwrap(rsa.wrap(aesKey)));
    }
}
