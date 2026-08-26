package com.example.tienda_tech.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;

    @Test
    void cspDisablesInlineScripts() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", containsString("script-src-attr 'none'")))
                .andExpect(header().string("Content-Security-Policy", containsString("style-src-attr 'none'")))
                .andExpect(header().string("Content-Security-Policy", not(containsString("'unsafe-inline'"))))
                .andExpect(header().string("Content-Security-Policy", not(containsString("script-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com 'unsafe-inline'"))));
    }

    @Test
    void siemRequiresAdminRole() throws Exception {
        String client = jwt.generateAccess(2, "cliente", "2", 5);
        String admin = jwt.generateAccess(1, "admin", "1", 5);

        mvc.perform(get("/api/siem/events")).andExpect(status().isForbidden());
        mvc.perform(get("/api/siem/events").header("Authorization", "Bearer " + client))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/siem/events").header("Authorization", "Bearer " + admin))
                .andExpect(status().isOk());
    }
}
