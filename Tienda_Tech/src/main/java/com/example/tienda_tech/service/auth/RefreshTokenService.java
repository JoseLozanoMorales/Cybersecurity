package com.example.tienda_tech.service.auth;

import com.example.tienda_tech.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.example.tienda_tech.repository.auth.RefreshTokenRepository;
import com.example.tienda_tech.model.auth.RefreshToken;

@Service @RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtUtil jwt;

    @Value("${auth.access.minutes}")   private int accessMinutes;
    @Value("${auth.absolute.hours}")   private int absoluteHours;
    @Value("${auth.idle.ADMIN}")      private int idleAdmin;
    @Value("${auth.idle.TRABAJADOR}") private int idleTrab;
    @Value("${auth.idle.CLIENTE}")    private int idleCli;

    private int idleForRole(String role) {
        return switch (role) {
            case "ADMIN" -> idleAdmin;
            case "TRABAJADOR" -> idleTrab;
            default -> idleCli;
        };
    }

    public record Tokens(String access){}

    public record IssueResult(String access, String refreshJwt, UUID jti, UUID family, Instant absExp){}

    public IssueResult issueOnLogin(Integer userId, String username, String role){
        var now   = Instant.now();
        var abs   = now.plusSeconds(absoluteHours*3600L);
        var jti   = UUID.randomUUID();
        var fam   = UUID.randomUUID();

        var access = jwt.generateAccess(userId, username, role, accessMinutes);
        var refreshJwt = jwt.generateRefresh(userId, role, jti, fam, abs);

        var rt = new RefreshToken();
        rt.setJti(jti); rt.setUserId(userId); rt.setRole(role);
        rt.setFamilyId(fam); rt.setIssuedAt(now); rt.setLastSeen(now);
        rt.setAbsoluteExp(abs); rt.setRevoked(false);
        repo.save(rt);

        return new IssueResult(access, refreshJwt, jti, fam, abs);
    }

    public record KeepalivePayload(long remainingIdleSeconds, long remainingAbsoluteSeconds){}

    @Transactional
    public KeepaliveResponse refresh(String refreshJwt){
        var claims = jwt.parse(refreshJwt).getBody();
        var jti = java.util.UUID.fromString(claims.getId());
        var role = String.valueOf(claims.get("role"));
        var userId = Integer.valueOf(claims.getSubject());
        var family = java.util.UUID.fromString(String.valueOf(claims.get("family_id")));

        var rt = repo.findByJtiAndRevokedFalse(jti)
                .orElseThrow(() -> new RuntimeException("refresh_revoked"));

        var now = java.time.Instant.now();
        if (now.isAfter(rt.getAbsoluteExp())) throw new RuntimeException("session_absolute_expired");

        int idleMin = idleForRole(role);     // <<< AQUÍ
        long remainingIdle = -1;
        if (idleMin > 0){
            long secs = idleMin*60L - (now.getEpochSecond() - rt.getLastSeen().getEpochSecond());
            if (secs <= 0) throw new RuntimeException("idle_timeout");
            remainingIdle = secs;
        }

        long remainingAbs = Math.max(0, rt.getAbsoluteExp().getEpochSecond() - now.getEpochSecond());

        // Rotación: revoco el viejo y creo uno nuevo en la misma familia
        rt.setRevoked(true); repo.save(rt);

        var newJti = UUID.randomUUID();
        var newRt = new RefreshToken();
        newRt.setJti(newJti);
        newRt.setUserId(userId);
        newRt.setRole(role);
        newRt.setFamilyId(family);
        newRt.setIssuedAt(now);
        newRt.setLastSeen(now);
        newRt.setAbsoluteExp(rt.getAbsoluteExp());
        newRt.setRevoked(false);
        repo.save(newRt);

        var access = jwt.generateAccess(userId, null, role, accessMinutes);
        var newRefreshJwt = jwt.generateRefresh(userId, role, newJti, family, rt.getAbsoluteExp());

        return new KeepaliveResponse(access, newRefreshJwt, new KeepalivePayload(remainingIdle, remainingAbs));
    }

    public record KeepaliveResponse(String access, String refreshJwt, KeepalivePayload meta){}

    @Transactional
    public void logoutFamily(UUID family){ repo.revokeFamily(family); }
}
