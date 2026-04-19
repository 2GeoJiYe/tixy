package com.tixy.api.payment.repository;

import com.tixy.api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByTsHash(String tsHash);
}
