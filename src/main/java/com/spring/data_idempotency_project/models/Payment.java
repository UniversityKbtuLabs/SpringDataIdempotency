package com.spring.data_idempotency_project.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.IndexColumn;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment")
public class Payment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @IndexColumn(name = "idx_idempotency_id")
    @Column(unique = true)
    private String idempotencyId;
    private Double amount;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private String description;
}
