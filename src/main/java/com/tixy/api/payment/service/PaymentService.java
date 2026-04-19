package com.tixy.api.payment.service;

import com.tixy.api.order.entity.Order;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.payment.dto.request.PaymentWebhookRequest;
import com.tixy.api.payment.dto.response.PaymentResponse;
import com.tixy.api.payment.repository.PaymentRepository;
import com.tixy.api.seat.entity.SeatSession;
import com.tixy.api.seat.service.SeatSessionService;
import com.tixy.core.exception.order.OrderErrorCode;
import com.tixy.core.exception.order.OrderException;
import com.tixy.core.exception.payment.PaymentErrorCode;
import com.tixy.core.exception.payment.PaymentException;
import com.tixy.core.exception.seat.SeatException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final SeatSessionService seatSessionService;
    private final PaymentFallbackService paymentFallbackService;
    private final PaymentDataService paymentDataService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse paymentProcess(PaymentWebhookRequest paymentWebhookRequest) {
        // 멱등 보장을 위해
        if (paymentRepository.existsByTsHash(paymentWebhookRequest.transactionId())) {
            throw new PaymentException(PaymentErrorCode.DUPLICATE_PAYMENT);
        }

        // order 정보 못찾는경우 핸들링
        Order order = paymentFallbackService.handleUnmatchedPayment(paymentWebhookRequest);
        if(order.getOrderStatus() != OrderStatus.PENDING){
            throw new OrderException(OrderErrorCode.CREAT_ORDER_FAILED); // TODO 에러 코드 확인
        }
        // 해당 주문의 좌석 세션들의 만료 체크
        // 해당 주문이 예약 가능시간이 만료 되었다면 해당 주문은 실패 및 결제 금액 포인트로 변경
        // 주문 정보 실패 처리
        List<SeatSession> seatSessions = seatSessionService.getSeatSessionsByOrderId(order.getId());
        try {
            for (SeatSession seatSession : seatSessions) {
                seatSession.checkExpired();
            }
            paymentFallbackService.isAmountValid(order.getTotalPrice(), paymentWebhookRequest);
        }catch (SeatException e) {
            paymentFallbackService.handleExpiredOrder(order, seatSessions, paymentWebhookRequest);
            throw new OrderException(OrderErrorCode.CREAT_ORDER_FAILED); // TODO 에러 코드 확인
        }catch (PaymentException e) {
            paymentDataService.addPointToWalletUser(paymentWebhookRequest);
            throw e;
        }

        paymentDataService.successPayment(paymentWebhookRequest ,order);

        return new PaymentResponse("sd");
    }
}
