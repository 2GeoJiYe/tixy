package com.tixy.core.util.datainit;

import com.tixy.core.exception.payment.PaymentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.tixy.core.exception.payment.PaymentErrorCode.NOT_MATCH_PAYMENT_SIGNATURE;

@Component
public class WebhookVerifier {

    @Value("${webhook.secret}")
    private String secret;

    //원래 hmac등 단방향 암호화를 사용하여 서명 검증을 진행해야 하지만.
    // 해당 사항은 구현 요구사항에 포함하지 않으므로 간단한 비교 검증만 진행 하도록 작성
    public void verify(String secret) {
        if(!secret.equals(this.secret)){
            throw new PaymentException(NOT_MATCH_PAYMENT_SIGNATURE);
        }
    }
}
