package com.example.tienda_tech.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SymmetricCryptoService {
    private static final String PREFIX = "v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int NONCE_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec keySpec;

    public SymmetricCryptoService(@Value("${app.crypto.key}") String secret) {
        this.keySpec = new SecretKeySpec(normalizeKey(secret), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BITS, nonce));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer payload = ByteBuffer.allocate(nonce.length + cipherText.length);
            payload.put(nonce);
            payload.put(cipherText);
            return PREFIX + Base64.getEncoder().encodeToString(payload.array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo cifrar el dato sensible", e);
        }
    }

    public String decrypt(String value) {
        if (!isEncrypted(value)) {
            return value;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(payload);

            byte[] nonce = new byte[NONCE_BYTES];
            buffer.get(nonce);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_BITS, nonce));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo descifrar el dato sensible", e);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private static byte[] normalizeKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("app.crypto.key es obligatorio");
        }

        byte[] decoded = tryDecodeBase64(secret);
        if (isValidAesKeyLength(decoded.length)) {
            return decoded;
        }
        if (isValidAesKeyLength(secret.getBytes(StandardCharsets.UTF_8).length)) {
            return secret.getBytes(StandardCharsets.UTF_8);
        }
        return sha256(secret);
    }

    private static byte[] tryDecodeBase64(String secret) {
        try {
            return Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ignored) {
            return new byte[0];
        }
    }

    private static boolean isValidAesKeyLength(int length) {
        return length == 16 || length == 24 || length == 32;
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
