package com.example.tienda_tech.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthenticationFilterTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesValidBearerAndIgnoresForgedIdentityHeader() throws Exception {
        JwtUtil jwt = new JwtUtil(SECRET);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwt.generateAccess(42, "ana", "1", 5));
        request.addHeader("X-User-Id", "999");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        assertEquals(42, AuthenticatedUser.id());
        assertTrue(AuthenticatedUser.hasRole("ADMIN"));
        assertEquals(42, request.getAttribute("usuarioId"));
    }

    @Test
    void rejectsInvalidBearer() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new JwtUtil(SECRET));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid.token.value");

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});

        assertThrows(IllegalStateException.class, AuthenticatedUser::id);
    }
}
