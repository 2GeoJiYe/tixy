package com.tixy.api.order.entity;

import com.tixy.api.member.entity.Member;
import com.tixy.api.order.enums.OrderStatus;
import com.tixy.api.ticket.entity.TicketType;
import com.tixy.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "orders")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String orderNo;

    @Column(nullable = false)
    private Long totalPrice; // 토큰...이라..소수점까지 관리를 해야할거같은데 일단 대기

    private int ticketCount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id" , nullable = false)
    private Member member;

    private String paidWalletAddress; // 들어와야 하는 지갑 주소 유저 지갑 스냅샷

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id" ,nullable = false)
    private TicketType ticketType;
}
