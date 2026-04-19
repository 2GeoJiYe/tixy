package com.tixy.api.payment.dto.request;


public record PaymentWebhookRequest (
        String transactionId,
        Object tokenInfo,
        Long blockTimestamp,
        String from,
        String to,
        String type, // USDT or TRON token
        Long value, // 토큰 수량,
        Long amount // 총 원화
){
}
