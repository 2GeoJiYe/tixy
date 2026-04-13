package com.tixy.api.ticket.repository;

import com.tixy.api.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    @Modifying
    @Query("UPDATE TicketType t SET t.ticketTypeStatus = 'ON_SALE' " +
            "WHERE t.ticketTypeStatus = 'PENDING' AND t.saleOpenDateTime <= :now")
    int updatePendingToOnSale(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE TicketType t SET t.ticketTypeStatus = 'SALE_ENDED' " +
            "WHERE t.ticketTypeStatus = 'ON_SALE' AND t.saleCloseDateTime <= :now")
    int updateOnSaleToSaleEnded(@Param("now") LocalDateTime now);
}
