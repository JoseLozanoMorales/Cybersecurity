package com.example.tienda_tech.service;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

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

    //NUEVO
    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private String buildPasswordChangeUrl() {
        // evita doble slash y apunta a tu página
        String base = (frontendBaseUrl == null) ? "" : frontendBaseUrl.trim();
        if (base.endsWith("/")) base = base.substring(0, base.length()-1);
        return base + "/ActualizarContrasenia.html";
    }

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
            otpCache.invalidate(cacheKey);
        }
        return ok;
    }

    public static class OtpTooManyRequestsException extends RuntimeException {
        public OtpTooManyRequestsException(String msg) { super(msg); }
    }


    /* ==================== NUEVO: envío de credenciales ==================== */

    // Conjunto "normal": mayúsculas, minúsculas, dígitos. Sin caracteres raros.
    private static final char[] UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray(); // sin I/O
    private static final char[] LOWER = "abcdefghijkmnopqrstuvwxyz".toCharArray(); // sin l
    private static final char[] DIGIT = "23456789".toCharArray(); // sin 0/1
    private static final char[] ALL;

    static {
        String all = new String(UPPER) + new String(LOWER) + new String(DIGIT);
        ALL = all.toCharArray();
    }

    /* Genera una contraseña legible (por defecto 12). Incluye al menos 1 mayúscula, 1 minúscula y 1 dígito. */
    public String generarPasswordLegible(int len) {
        int L = Math.max(8, Math.min(len <= 0 ? 12 : len, 32)); // 8..32
        List<Character> buf = new ArrayList<>(L);

        // Garantizar clases
        buf.add(UPPER[RAND.nextInt(UPPER.length)]);
        buf.add(LOWER[RAND.nextInt(LOWER.length)]);
        buf.add(DIGIT[RAND.nextInt(DIGIT.length)]);

        while (buf.size() < L) {
            buf.add(ALL[RAND.nextInt(ALL.length)]);
        }

        // Mezclar
        Collections.shuffle(buf, RAND);

        StringBuilder sb = new StringBuilder(L);
        for (char c : buf) sb.append(c);
        return sb.toString();
    }

    /* Envía un correo con las credenciales (usuario + password en claro). Lanza excepción si falla. */
    public void enviarCredenciales(String correo, String usuario, String passwordPlano) {

        // 1) Genera token y URL con token
        String token = emitirTokenCambioPassword(correo);
        String urlCambio = buildPasswordChangeUrl() + "?t=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(correo);
        msg.setSubject("Tus credenciales de acceso - TiendaTech");
        msg.setText("""
        ¡Hola!

        Se ha creado tu cuenta en TiendaTech.

        Usuario: %s
        Contraseña temporal: %s

        Por seguridad, debes cambiar tu contraseña:
        %s

        Si no solicitaste esta cuenta, ignora este correo.

        Saludos,
        TiendaTech
        """.formatted(
                (usuario == null || usuario.isBlank()) ? "(asignado por sistema)" : usuario,
                passwordPlano,
                urlCambio
        ));

        mailSender.send(msg); // si falla, lanzará una excepción  buildPasswordChangeUrl()
    }

    /*Genera una contraseña legible y la envía por correo. */
    public String generarYEnviarCredenciales(String correo, String usuario, Integer length) {
        int L = (length == null ? 12 : length.intValue());
        String pwd = generarPasswordLegible(L);
        enviarCredenciales(correo, usuario, pwd); // si esto falla, que reviente
        return pwd;
    }

    //nuevooooo


    public void enviarPasswordTemporalRecuperacion(String correo, String usuario, String passwordPlano) {
        String token = emitirTokenCambioPassword(correo);
        String urlCambio = buildPasswordChangeUrl() + "?t=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(correo);
        msg.setSubject("Recuperación de contraseña - TiendaTech");
        msg.setText("""
            ¡Hola %s!

            Generamos una contraseña temporal para que puedas ingresar:

            %s

            Luego, cámbiala aquí:
            %s

            Si no solicitaste este cambio, ignora este correo o contáctanos.

            -- TiendaTech
            """.formatted(
                (usuario == null || usuario.isBlank()) ? "usuario" : usuario,
                passwordPlano,
                urlCambio
        ));
        mailSender.send(msg);
    }

    // inyecta el cache:
    private final @Qualifier("pwdChangeCache") Cache<String, String> pwdChangeCache;

    // emitir/consumir
    public String emitirTokenCambioPassword(String correo) {
        String token = java.util.UUID.randomUUID().toString().replace("-", "");
        pwdChangeCache.put(token, correo.toLowerCase());
        return token;
    }

    public String peekTokenCambioPassword(String token) {
        return pwdChangeCache.getIfPresent(token); // NO invalida
    }
    public void invalidateTokenCambioPassword(String token) {
        pwdChangeCache.invalidate(token);
    }
}
