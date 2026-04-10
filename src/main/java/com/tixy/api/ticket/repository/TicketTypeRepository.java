package com.tixy.api.ticket.repository;

import com.tixy.api.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
}
