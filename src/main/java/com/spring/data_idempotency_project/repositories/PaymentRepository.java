package com.spring.data_idempotency_project.repositories;

import com.spring.data_idempotency_project.models.Payment;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Cacheable(value = "payments", key = "#idempotencyId", unless = "#result == null")
    Payment findByIdempotencyId(String idempotencyId);

    @CachePut(value = "payments", key = "#payment.idempotencyId")
    @Override
    <S extends Payment> S save(S payment);
}
