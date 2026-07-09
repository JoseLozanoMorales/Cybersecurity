package com.example.tienda_tech.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserOnlineService {
    @Autowired(required = false) private SessionRegistry sessionRegistry;

    public boolean isOnline(String username){
        if (username == null || username.isBlank() || sessionRegistry == null) return false;
        return sessionRegistry.getAllPrincipals().stream().anyMatch(p ->
                (p instanceof UserDetails ud && ud.getUsername().equalsIgnoreCase(username)) ||
                        (!(p instanceof UserDetails) && p.toString().equalsIgnoreCase(username))
        );
    }
}
