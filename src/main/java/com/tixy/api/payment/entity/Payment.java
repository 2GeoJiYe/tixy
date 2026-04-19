package com.tixy.api.payment.entity;

import com.tixy.api.order.entity.Order;
import com.tixy.api.payment.enums.PaymentStatus;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String paymentNo;

    @Column(nullable = false, unique = true)
    private String tsHash;

    private PaymentStatus paymentStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long payAmount; // 입금 당시 계산한 금액(원)

    @Column(nullable = false)
    private Long payValue; // 입급된 토큰 양

    @Column(nullable = false)
    private String senderWalletAddress;

    @Column(nullable = false)
    private LocalDateTime depositAt;

}
