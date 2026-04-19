package com.tixy.api.payment.service;

import com.tixy.api.order.entity.Order;
import com.tixy.api.order.service.OrderService;
import com.tixy.api.payment.dto.request.PaymentWebhookRequest;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.core.exception.MemberException;
import com.tixy.core.exception.payment.PaymentErrorCode;
import com.tixy.core.exception.payment.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentFallbackService {
    private final PaymentDataService paymentDataService;
    private final OrderService orderService;

    public Order handleUnmatchedPayment(PaymentWebhookRequest request) {
        // 받아온 정보를 토대로 주문정보 가져오기 (가장 최근 1개)
        Optional<Order> maybeOrder = orderService.getOrderBySenderWalletAddress(request.from());

        if (maybeOrder.isEmpty()) {
            try {
                // 주문정보를 못찾을 경우 해당 금액만큼 지갑 주인에게 포인트로 보상
                paymentDataService.addPointToWalletUser(request);
                throw new PaymentException(PaymentErrorCode.MATCHED_ORDER_NOT_FOUND);
            } catch (MemberException e) {
                // 만약 지갑주소 주인도 못찾으면 수수료룰 제외한 금액을 환불 해야하는데, 상태를 환불 예정으로 일단 저장하고 스케줄러로 일관 환불처리
                paymentDataService.saveRefundPayment(request);
                throw new PaymentException(PaymentErrorCode.MATCHED_WALLET_NOT_FOUND);
            }
        }

        return maybeOrder.get();
    }

    public void handleExpiredOrder(Order order, List<SeatSession> seatSessions, PaymentWebhookRequest request) {
        seatSessions.forEach(SeatSession::unHeld);
        order.expireOrder();
        paymentDataService.addPointToWalletUser(request);
    }

    public void isAmountValid(Long expectedAmount, PaymentWebhookRequest request) {
        if(Math.abs(expectedAmount - request.amount()) >= 1000){
            throw new PaymentException(PaymentErrorCode.NOT_MATCH_PAYMENT_AMOUNT);
        }
    }
}
