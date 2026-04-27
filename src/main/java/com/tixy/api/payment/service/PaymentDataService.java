package com.tixy.api.payment.service;

import com.tixy.api.member.entity.Member;
import com.tixy.api.member.service.MemberService;
import com.tixy.api.order.entity.Order;
import com.tixy.api.payment.dto.request.PaymentWebhookRequest;
import com.tixy.api.payment.entity.Payment;
import com.tixy.api.payment.enums.PaymentStatus;
import com.tixy.api.payment.repository.PaymentRepository;
import com.tixy.core.exception.MemberException;
import com.tixy.core.util.DateTimeUtils;
import com.tixy.core.util.PublicIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.tixy.core.exception.MemberErrorCode.MEMBER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentDataService {
    public final static String PAYMENT_PREFIX = "PAY";
    private final PaymentRepository paymentRepository;
    private final MemberService memberService;

    public void successPayment(PaymentWebhookRequest paymentWebhookRequest, Order order) {
        order.confirmOrder();
        Payment payment = Payment.builder()
                .paymentNo(PublicIdGenerator.generate(PAYMENT_PREFIX))
                .tsHash(paymentWebhookRequest.transactionId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .payAmount(paymentWebhookRequest.amount())
                .payValue(paymentWebhookRequest.value())
                .senderWalletAddress(order.getSenderWalletAddress())
                .depositAt(DateTimeUtils.epochMilliToKst(paymentWebhookRequest.blockTimestamp()))
                .order(order)
                .build();
        paymentRepository.save(payment);
    }

    // 주문 정보를 찾지못해서 회사 지갑에서 고객 지갑으로 재 송금시 수수료 만큼에 금액적 손해 발생
    // 해당 금액만큼 포인트화 작업하여 내부 서비스에서 현금 처럼 사용하도록 유도
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addPointToWalletUser(PaymentWebhookRequest paymentWebhookRequest) {
        Member member = memberService.findByWalletAddress(paymentWebhookRequest.from())
                .orElseThrow(() -> new MemberException(MEMBER_NOT_FOUND));

        // 고객에게 들어온 금액만큼 포인트 적립
        member.addPoint(paymentWebhookRequest.amount());

        // order를 제외하고 저장.
        Payment payment = Payment.builder()
                .paymentNo(PublicIdGenerator.generate(PAYMENT_PREFIX))
                .tsHash(paymentWebhookRequest.transactionId())
                .payAmount(paymentWebhookRequest.amount())
                .payValue(paymentWebhookRequest.value())
                .senderWalletAddress(paymentWebhookRequest.from())
                .depositAt(DateTimeUtils.epochMilliToKst(paymentWebhookRequest.blockTimestamp()))
                .paymentStatus(PaymentStatus.POINT_REWARDED) // 포인트 보상
                .build();
        paymentRepository.save(payment);
    }

    // 만약 주문도 못찾고, 포인트화 시킬 지갑 주소도 우리 고객중 없다면, 해당건에 대하여 환불 예정 상태로 변경.
    // 스케줄러를 통해 환불을 진행. -> 너무 큰 금액에 대해서는 정책필요.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRefundPayment(PaymentWebhookRequest paymentWebhookRequest){
        Payment payment = Payment.builder()
                .paymentNo(PublicIdGenerator.generate(PAYMENT_PREFIX))
                .tsHash(paymentWebhookRequest.transactionId())
                .payAmount(paymentWebhookRequest.amount())
                .payValue(paymentWebhookRequest.value())
                .senderWalletAddress(paymentWebhookRequest.from())
                .depositAt(DateTimeUtils.epochMilliToKst(paymentWebhookRequest.blockTimestamp()))
                .paymentStatus(PaymentStatus.WALLET_ERROR) // 환불 예정 상태
                .build();
        paymentRepository.save(payment);
    }
}
