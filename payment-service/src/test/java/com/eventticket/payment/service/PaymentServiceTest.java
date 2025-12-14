package com.eventticket.payment.service;

import com.eventticket.payment.entity.PaymentTransaction;
import com.eventticket.payment.repository.PaymentTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentTransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = new PaymentTransaction();
        testTransaction.setId("payment-123");
        testTransaction.setUserId("user-001");
        testTransaction.setEventId("event-456");
        testTransaction.setAmount(100.0);
        testTransaction.setPaymentMethod("CREDIT_CARD");
        testTransaction.setStatus("PENDING");
    }

    @Test
    void testCreatePayment_Success() {
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);

        String paymentId = paymentService.createPayment("user-001", "event-456", 100.0, "CREDIT_CARD");

        assertNotNull(paymentId);
        assertEquals("payment-123", paymentId);
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
    }

    @Test
    void testConfirmPayment_Success() {
        testTransaction.setStatus("PENDING");
        when(transactionRepository.findById("payment-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);

        paymentService.confirmPayment("payment-123", "gateway-txn-123");

        verify(transactionRepository, times(1)).findById("payment-123");
        verify(transactionRepository, times(1)).save(any(PaymentTransaction.class));
        verify(kafkaTemplate, times(1)).send("payment-confirmed", "payment-123", 
                "{\"paymentId\": \"payment-123\", \"userId\": \"user-001\", \"eventId\": \"event-456\", \"amount\": 100.00}");
    }

    @Test
    void testConfirmPayment_NotFound() {
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.confirmPayment("nonexistent", "gateway-txn-123"));
    }

    @Test
    void testGetPaymentStatus_Success() {
        when(transactionRepository.findById("payment-123")).thenReturn(Optional.of(testTransaction));

        PaymentTransaction result = paymentService.getPaymentStatus("payment-123");

        assertNotNull(result);
        assertEquals("payment-123", result.getId());
        assertEquals("PENDING", result.getStatus());
        verify(transactionRepository, times(1)).findById("payment-123");
    }

    @Test
    void testGetPaymentStatus_NotFound() {
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> paymentService.getPaymentStatus("nonexistent"));
    }

    @Test
    void testConfirmPayment_WithFailedValidation() {
        testTransaction.setStatus("PENDING");
        when(transactionRepository.findById("payment-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);

        // Gateway transaction ID contains "FAIL" keyword, so validation will fail
        paymentService.confirmPayment("payment-123", "gateway-FAIL-123");

        verify(kafkaTemplate, times(1)).send(eq("payment-failed"), eq("payment-123"), anyString());
    }

    @Test
    void testConfirmPayment_ValidatePaymentGateway() {
        testTransaction.setStatus("PENDING");
        when(transactionRepository.findById("payment-123")).thenReturn(Optional.of(testTransaction));
        when(transactionRepository.save(any(PaymentTransaction.class))).thenReturn(testTransaction);

        // Valid gateway transaction ID
        paymentService.confirmPayment("payment-123", "valid-gateway-txn");

        verify(kafkaTemplate, times(1)).send(eq("payment-confirmed"), eq("payment-123"), anyString());
    }
}
