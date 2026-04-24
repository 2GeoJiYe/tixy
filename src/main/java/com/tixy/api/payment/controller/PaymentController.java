package com.tixy.api.payment.controller;

import com.tixy.api.payment.dto.request.PaymentWebhookRequest;
import com.tixy.api.payment.dto.response.PaymentResponse;
import com.tixy.api.payment.service.PaymentService;
import com.tixy.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/tixy/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/v1/webhook")
    public ResponseEntity<ApiResponse<PaymentResponse>> webhook(@RequestBody PaymentWebhookRequest body){
        log.info("webhook request: {}", body);
        PaymentResponse response = paymentService.paymentProcess(body);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
