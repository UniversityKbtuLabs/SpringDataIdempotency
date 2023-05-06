package com.spring.data_idempotency_project.repositories;

import com.spring.data_idempotency_project.models.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    Optional<User> findByEmail(String email);
}
