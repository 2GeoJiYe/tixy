package com.tixy.api.order.repository;

import com.tixy.api.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Query("SELECT o FROM Order o WHERE o.senderWalletAddress = :walletAddress AND o.orderStatus ='PENDING' ORDER BY o.createdAt DESC LIMIT 1")
    Optional<Order> findPendingOrderByWalletAddress(@Param("walletAddress")String walletAddress);

    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.member.id = :memberId AND o.ticketType.id = :ticketTypeId AND o.orderStatus = 'PENDING'")
    boolean existsPendingTicket(@Param("memberId") Long memberId, @Param("ticketTypeId") Long ticketTypeId);
}
