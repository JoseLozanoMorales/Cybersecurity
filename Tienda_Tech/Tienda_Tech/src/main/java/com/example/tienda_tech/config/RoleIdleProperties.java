package com.example.tienda_tech.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "auth")
public class RoleIdleProperties {
    private Map<String, Integer> idle;
    public int minutesFor(String roleName){
        if (roleName == null || idle == null) return 0;
        return idle.getOrDefault(roleName.toUpperCase(), 0);
    }
    public Map<String, Integer> getIdle(){ return idle; }
    public void setIdle(Map<String, Integer> idle){ this.idle = idle; }
}
