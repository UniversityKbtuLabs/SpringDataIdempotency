package com.spring.data_idempotency_project.repositories;

import com.spring.data_idempotency_project.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIdempotencyId(String idempotencyId);
}
