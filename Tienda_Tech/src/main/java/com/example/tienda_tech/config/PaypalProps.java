package com.example.tienda_tech.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter @Setter
@Component
@ConfigurationProperties(prefix = "paypal")
public class PaypalProps {
    private String mode;         // sandbox | live
    private String clientId;
    private String clientSecret;
    private String currency = "USD";

    public String getApiBase() {
        return "sandbox".equalsIgnoreCase(mode)
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }
}
