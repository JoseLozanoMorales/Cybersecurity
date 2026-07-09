package com.example.tienda_tech.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SymmetricCryptoServiceTest {

    @Test
    void encryptUsesDifferentNonceAndDecryptsOriginalValue() {
        SymmetricCryptoService crypto = new SymmetricCryptoService("0123456789abcdef0123456789abcdef");

        String first = crypto.encrypt("4111111111111111");
        String second = crypto.encrypt("4111111111111111");

        assertTrue(crypto.isEncrypted(first));
        assertTrue(crypto.isEncrypted(second));
        assertNotEquals(first, second);
        assertEquals("4111111111111111", crypto.decrypt(first));
        assertEquals("4111111111111111", crypto.decrypt(second));
    }
}
