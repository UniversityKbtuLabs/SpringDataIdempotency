package com.spring.data_idempotency_project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.data_idempotency_project.dto.PaymentCreateDTO;
import com.spring.data_idempotency_project.models.Payment;
import com.spring.data_idempotency_project.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.PartitionOffset;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public String createPayment(PaymentCreateDTO paymentCreateDTO) throws JsonProcessingException {
        Payment payment = null;
        try {
            payment = paymentRepository.findByIdempotencyId(paymentCreateDTO.getIdempotencyId());
        } catch (Exception e) {

        } finally {
            if (payment != null) {
                return "Идемпотентность успешно";
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                kafkaTemplate.send(
                        "my_topic",
                        0,
                        String.valueOf(paymentCreateDTO.getIdempotencyId()),
                        objectMapper.writeValueAsBytes(paymentCreateDTO)
                );
                return "Отправлено на кафка";
            }
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
        try {
            paymentRepository.save(payment);
            savePendingPayments();
            redisTemplate.delete("payments_need_save");
        } catch (Exception e) {
            redisTemplate.opsForList().rightPush("payments_need_save", payment);
        }
    }

    private void savePendingPayments() {
        List<Object> pendingPayments = redisTemplate.opsForList().range("payments_need_save", 0, -1);
        System.out.println(pendingPayments);
        for (Object payment : pendingPayments) {
            Payment newPayment = (Payment) payment;
            try {
                paymentRepository.save(newPayment); // записываем платеж в базу
            } catch (Exception e) {

            }
        }
    }
}
