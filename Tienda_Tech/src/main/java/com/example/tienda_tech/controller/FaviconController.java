package com.example.tienda_tech.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {
    @RequestMapping("favicon.ico")
    public void favicon() {
        // 200 vacío — evita logs ruidosos
    }
}
