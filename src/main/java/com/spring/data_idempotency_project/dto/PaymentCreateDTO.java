package com.spring.data_idempotency_project.dto;

import com.spring.data_idempotency_project.models.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCreateDTO {
    private String idempotencyId;
    private Double amount;
    private Currency currency;
    private String description;
}
