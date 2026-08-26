package com.example.tienda_tech.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA se usa únicamente para envolver secretos pequeños, como una clave AES.
 * Los datos de negocio continúan cifrándose con AES-GCM.
 */
@Service
public class RsaOaepService {
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private final KeyPair keyPair;

    public RsaOaepService(
            @Value("${app.crypto.rsa.private-key:}") String privateKey,
            @Value("${app.crypto.rsa.public-key:}") String publicKey) {
        try {
            this.keyPair = loadOrGenerate(privateKey, publicKey);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("RSA no disponible", e);
        }
    }

    private KeyPair loadOrGenerate(String privateValue, String publicValue) throws GeneralSecurityException {
        if (!privateValue.isBlank() && !publicValue.isBlank()) {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(decodeKey(privateValue)));
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(decodeKey(publicValue)));
            return new KeyPair(publicKey, privateKey);
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(3072);
        return generator.generateKeyPair();
    }

    private byte[] decodeKey(String value) {
        String normalized = value.replaceAll("-----BEGIN [^-]+-----|-----END [^-]+-----|\\s", "");
        return Base64.getDecoder().decode(normalized);
    }

    public String wrap(byte[] secret) {
        if (secret == null || secret.length == 0) throw new IllegalArgumentException("Secreto vacío");
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            return Base64.getEncoder().encodeToString(cipher.doFinal(secret));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo envolver la clave", e);
        }
    }

    public byte[] unwrap(String wrappedSecret) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            return cipher.doFinal(Base64.getDecoder().decode(wrappedSecret));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("No se pudo desenvolver la clave", e);
        }
    }

    public String publicKeyPem() {
        String encoded = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(keyPair.getPublic().getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + encoded + "\n-----END PUBLIC KEY-----";
    }
}
