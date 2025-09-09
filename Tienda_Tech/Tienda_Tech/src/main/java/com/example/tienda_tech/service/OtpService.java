package com.example.tienda_tech.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final JavaMailSender mailSender;
    private final @Qualifier("otpCache") Cache<String, String> otpCache;           // key: email:txId -> hash
    private final @Qualifier("attemptsCache") Cache<String, Integer> attemptsCache; // key: rate:email -> count

    @Value("${otp.ttl-minutes:10}")
    private int otpTtlMin;

    @Value("${otp.length:6}")
    private int otpLen;

    @Value("${otp.resend.cooldown-seconds:60}")
    private int resendCooldownSeconds;

    @Value("${otp.rate.max-per-15min:3}")
    private int maxPerWindow;

    private static final SecureRandom RAND = new SecureRandom();

    private static String key(String email, String txId) { return email + ":" + txId; }
    private static String rateKey(String email) { return "rate:" + email.toLowerCase(); }

    private boolean canSendForEmail(String email) {
        String k = rateKey(email);
        Integer current = attemptsCache.getIfPresent(k);
        if (current == null) current = 0;
        if (current >= maxPerWindow) return false;
        attemptsCache.put(k, current + 1);
        return true;
    }

    public Map<String, Object> enviar(String email, String txIdReuso) {
        if (!canSendForEmail(email)) {
            throw new OtpTooManyRequestsException("Demasiados envíos para este correo. Intenta más tarde.");
        }

        String txId = (txIdReuso != null && !txIdReuso.isBlank()) ? txIdReuso : UUID.randomUUID().toString();
        int bound = (int) Math.pow(10, otpLen);
        String code = String.format("%0" + otpLen + "d", RAND.nextInt(bound));

        String hash = BCrypt.hashpw(code, BCrypt.gensalt());
        String cacheKey = key(email, txId);
        otpCache.put(cacheKey, hash);

        // Email
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Código de verificación - TiendaTech");
        msg.setText("""
                Tu código de verificación es: %s
                Expira en %d minutos.
                Si no solicitaste este código, ignora este correo.
                """.formatted(code, otpTtlMin));

        try {
            mailSender.send(msg);
            log.info("OTP enviado a {} txId={}", email, txId);
        } catch (Exception ex) {
            // no dejes un OTP huérfano si el correo falló
            otpCache.invalidate(cacheKey);
            log.error("Error enviando OTP a {}: {}", email, ex.getMessage(), ex);
            // Propaga un error claro (el front ya lo muestra)
            throw new RuntimeException("MAIL_SEND_FAILED: " + ex.getMessage(), ex);
        }

        return Map.of(
                "txId", txId,
                "correo", email,
                "expiresInMin", otpTtlMin,
                "resendCooldownSec", resendCooldownSeconds,
                "now", Instant.now().toString()
        );
    }

    public boolean validar(String email, String code, String txId) {
        String cacheKey = key(email, txId);
        String hash = otpCache.getIfPresent(cacheKey);
        if (hash == null) return false; // expirado / inexistente
        boolean ok = BCrypt.checkpw(code, hash);
        if (ok) {
            otpCache.invalidate(cacheKey); // one-time use
            // Aquí puedes activar al usuario en BD si quieres:
            // usuarioRepository.activateByEmail(email);
        }
        return ok;
    }

    public static class OtpTooManyRequestsException extends RuntimeException {
        public OtpTooManyRequestsException(String msg) { super(msg); }
    }
}
