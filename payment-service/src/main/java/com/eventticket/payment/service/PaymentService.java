package com.eventticket.payment.service;

import com.eventticket.payment.entity.PaymentTransaction;
import com.eventticket.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
      private final PaymentTransactionRepository transactionRepository;
      private final KafkaTemplate<String, String> kafkaTemplate;

      private static final String PAYMENT_CONFIRMED_TOPIC = "payment-confirmed";
      private static final String PAYMENT_FAILED_TOPIC = "payment-failed";

      public String createPayment(String userId, String eventId, double amount, String paymentMethod) {
            log.info("Creating payment: userId={}, eventId={}, amount={}", userId, eventId, amount);

            PaymentTransaction transaction = PaymentTransaction.builder()
                        .userId(userId)
                        .eventId(eventId)
                        .amount(amount)
                        .paymentMethod(paymentMethod)
                        .status("PENDING")
                        .build();

            PaymentTransaction saved = transactionRepository.save(transaction);
            log.info("Payment created with status PENDING: paymentId={}", saved.getId());

            return saved.getId();
      }

      public void confirmPayment(String paymentId, String transactionIdFromGateway) {
            log.info("Confirming payment: paymentId={}, gatewayTransactionId={}", paymentId, transactionIdFromGateway);

            PaymentTransaction transaction = transactionRepository.findById(paymentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            // Simulate payment gateway call (in production, verify with actual gateway)
            boolean isValid = validatePaymentWithGateway(transactionIdFromGateway);

            if (isValid) {
                  transaction.setStatus("CONFIRMED");
                  transaction.setTransactionId(transactionIdFromGateway);
                  transactionRepository.save(transaction);

                  // Publish event
                  publishPaymentConfirmedEvent(transaction);
                  log.info("Payment confirmed: paymentId={}", paymentId);
            } else {
                  transaction.setStatus("FAILED");
                  transactionRepository.save(transaction);
                  log.warn("Payment validation failed: paymentId={}", paymentId);
                  publishPaymentFailedEvent(transaction);
            }
      }

      public PaymentTransaction getPaymentStatus(String paymentId) {
            return transactionRepository.findById(paymentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
      }

      private boolean validatePaymentWithGateway(String transactionId) {
            // Mock validation - in production, call real payment gateway API
            log.info("Validating payment with gateway: transactionId={}", transactionId);
            return !transactionId.contains("FAIL");
      }

      private void publishPaymentConfirmedEvent(PaymentTransaction transaction) {
            String message = String.format(
                        "{\"paymentId\": \"%s\", \"userId\": \"%s\", \"eventId\": \"%s\", \"amount\": %.2f}",
                        transaction.getId(), transaction.getUserId(), transaction.getEventId(),
                        transaction.getAmount());

            kafkaTemplate.send(PAYMENT_CONFIRMED_TOPIC, transaction.getId(), message);
            log.info("Payment confirmed event published to Kafka");
      }

      private void publishPaymentFailedEvent(PaymentTransaction transaction) {
            String message = String.format("{\"paymentId\": \"%s\", \"userId\": \"%s\"}",
                        transaction.getId(), transaction.getUserId());

            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, transaction.getId(), message);
            log.info("Payment failed event published to Kafka");
      }
}
