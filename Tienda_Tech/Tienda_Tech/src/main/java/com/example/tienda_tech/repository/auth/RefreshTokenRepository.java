package com.example.tienda_tech.repository.auth;

import org.springframework.data.jpa.repository.*;
import java.util.*;
import com.example.tienda_tech.model.auth.RefreshToken;
import com.example.tienda_tech.repository.auth.RefreshTokenRepository;
import com.example.tienda_tech.security.JwtUtil;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByJtiAndRevokedFalse(java.util.UUID jti);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.familyId = ?1")
    void revokeFamily(java.util.UUID familyId);
}
