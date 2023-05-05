package com.spring.data_idempotency_project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.data_idempotency_project.dto.PaymentCreateDTO;
import com.spring.data_idempotency_project.models.Payment;
import com.spring.data_idempotency_project.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    public String createPayment(PaymentCreateDTO paymentCreateDTO) throws JsonProcessingException {
        Optional<Payment> payment = paymentRepository.findByIdempotencyId(paymentCreateDTO.getIdempotencyId());
        if (payment.isPresent()) {
            return "Идемпотентность успешно";
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            kafkaTemplate.send(
                    "my_topic",
                    0,
                    paymentCreateDTO.getIdempotencyId(),
                    objectMapper.writeValueAsBytes(paymentCreateDTO)
            );
            return "Отправлено на кафка";
        }
    }

    @KafkaListener(topicPartitions = @TopicPartition(
            topic = "my_topic", partitionOffsets = @PartitionOffset(partition = "0", initialOffset = "0")
    ))
    public void listenPaymentCreate(String message) throws JsonProcessingException {
        PaymentCreateDTO paymentCreateDTO = new ObjectMapper().readValue(message, PaymentCreateDTO.class);
        Payment payment = Payment.builder()
                .idempotencyId(paymentCreateDTO.getIdempotencyId())
                .amount(paymentCreateDTO.getAmount())
                .currency(paymentCreateDTO.getCurrency())
                .description(paymentCreateDTO.getDescription())
                .build();
        paymentRepository.save(payment);
    }
}
