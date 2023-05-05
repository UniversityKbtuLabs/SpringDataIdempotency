package com.spring.data_idempotency_project.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.spring.data_idempotency_project.dto.PaymentCreateDTO;
import com.spring.data_idempotency_project.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/payments")
    public ResponseEntity<String> sayPay() {
        return ResponseEntity.ok("Payment successful");
    }

    @PostMapping("/payments")
    public ResponseEntity<String> createPayment(@RequestBody PaymentCreateDTO paymentCreateDTO) throws JsonProcessingException {
        return new ResponseEntity<>(paymentService.createPayment(paymentCreateDTO), HttpStatus.OK);
    }
}
