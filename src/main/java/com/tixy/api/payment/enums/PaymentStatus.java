package com.tixy.api.payment.enums;

public enum PaymentStatus {
    SUCCESS,
    FAIL,
    REFUNDED,
    EXPIRED, // 예약 좌석 만료됨.
    POINT_REWARDED,
    AMOUNT_MISMATCH,
    WALLET_ERROR, // 환불 대상
    SUSPICIOUS // 의심 거래
}
