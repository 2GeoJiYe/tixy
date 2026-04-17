package com.tixy.api.payment.dto.request;


public record PaymentWebhookRequest (
        String transactionId,
        Object tokenInfo,
        Long blockTimestamp,
        String from,
        String to,
        String type,
        Long value,
        Long amount
){
}
