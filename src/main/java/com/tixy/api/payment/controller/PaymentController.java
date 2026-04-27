package com.tixy.api.payment.controller;

import com.tixy.api.payment.dto.request.PaymentWebhookRequest;
import com.tixy.api.payment.dto.response.PaymentResponse;
import com.tixy.api.payment.service.PaymentService;
import com.tixy.core.dto.ApiResponse;
import com.tixy.core.util.datainit.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/tixy/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookVerifier webhookVerifier;


    @PostMapping("/v1/webhook")
    public ResponseEntity<ApiResponse<PaymentResponse>> webhook(
            @RequestHeader("X-Watcher-Signature") String signature,
            @RequestBody PaymentWebhookRequest body){

        webhookVerifier.verify(signature);

        PaymentResponse response = paymentService.paymentProcess(body);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }
}
