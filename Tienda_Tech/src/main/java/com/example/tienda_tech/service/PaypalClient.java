package com.example.tienda_tech.service;

import com.example.tienda_tech.config.PaypalProps;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class PaypalClient {

    private final PaypalProps props;
    private final ObjectMapper om;
    private final RestTemplate rt;

    private String bearerToken() throws Exception {
        String url = props.getApiBase() + "/v1/oauth2/token";
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(props.getClientId(), props.getClientSecret());
        h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","client_credentials");
        String json = rt.postForObject(url, new HttpEntity<>(body, h), String.class);
        return om.readTree(json).path("access_token").asText();
    }

    /** NECESARIO para Hosted Fields */
    public String generateClientToken() throws Exception {
        String url = props.getApiBase() + "/v1/identity/generate-token";
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken());
        h.setContentType(MediaType.APPLICATION_JSON);
        String json = rt.postForObject(url, new HttpEntity<>("{}", h), String.class);
        return om.readTree(json).path("client_token").asText();
    }

    /** Crea orden y devuelve orderId */
    public String createOrder(BigDecimal total, String reference) throws Exception {
        String url = props.getApiBase() + "/v2/checkout/orders";
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken());
        h.setContentType(MediaType.APPLICATION_JSON);

        ObjectNode req = om.createObjectNode();
        req.put("intent","CAPTURE");
        var purchase = om.createObjectNode();
        var amount   = om.createObjectNode();
        amount.put("currency_code", props.getCurrency());
        amount.put("value", total.setScale(2, RoundingMode.HALF_UP).toPlainString());
        purchase.set("amount", amount);
        purchase.put("reference_id", reference);
        req.set("purchase_units", om.createArrayNode().add(purchase));

        String json = rt.postForObject(url, new HttpEntity<>(req.toString(), h), String.class);
        return om.readTree(json).path("id").asText();
    }

    /** Captura la orden y devuelve el JSON completo (status, etc.) */
    public JsonNode captureOrder(String orderId) throws Exception {
        String url = props.getApiBase() + "/v2/checkout/orders/" + orderId + "/capture";
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(bearerToken());
        h.setContentType(MediaType.APPLICATION_JSON);
        String json = rt.postForObject(url, new HttpEntity<>("{}", h), String.class);
        return om.readTree(json);
    }
}