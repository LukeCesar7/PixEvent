package com.pixevent.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 Inerte - Em desenvolvimento
 */
@RestController
@RequestMapping("/webhook")
public class WebhookController {

    @PostMapping("/mp")
    public ResponseEntity<Void> mercadoPago() {
        return ResponseEntity.ok().build();
    }
}
