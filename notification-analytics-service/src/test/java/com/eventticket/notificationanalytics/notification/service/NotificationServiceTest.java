package com.eventticket.notificationanalytics.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void testNotificationServiceInitialized() {
        assertNotNull(notificationService);
    }

    @Test
    void testOnTicketCreated_ValidMessage() {
        String validMessage = "{\"ticketId\": \"ticket-123\", \"userId\": \"user-001\", \"qrCode\": \"QR_CODE_123\"}";
        
        assertDoesNotThrow(() -> notificationService.onTicketCreated(validMessage));
    }

    @Test
    void testOnTicketCreated_EmptyMessage() {
        String emptyMessage = "";
        
        assertDoesNotThrow(() -> notificationService.onTicketCreated(emptyMessage));
    }

    @Test
    void testOnTicketCreated_NullMessage() {
        assertDoesNotThrow(() -> notificationService.onTicketCreated(null));
    }

    @Test
    void testOnPaymentFailed_ValidMessage() {
        String validMessage = "{\"paymentId\": \"payment-123\", \"userId\": \"user-001\"}";
        
        assertDoesNotThrow(() -> notificationService.onPaymentFailed(validMessage));
    }

    @Test
    void testOnPaymentFailed_EmptyMessage() {
        String emptyMessage = "";
        
        assertDoesNotThrow(() -> notificationService.onPaymentFailed(emptyMessage));
    }

    @Test
    void testOnPaymentFailed_NullMessage() {
        assertDoesNotThrow(() -> notificationService.onPaymentFailed(null));
    }

    @Test
    void testKafkaListenerResilience_TicketCreated() {
        // Test multiple consecutive calls
        for (int i = 0; i < 5; i++) {
            int iteration = i;
            assertDoesNotThrow(() -> 
                    notificationService.onTicketCreated("{\"ticketId\": \"ticket-" + iteration + "\"}"));
        }
    }

    @Test
    void testKafkaListenerResilience_PaymentFailed() {
        // Test multiple consecutive calls
        for (int i = 0; i < 5; i++) {
            int iteration = i;
            assertDoesNotThrow(() -> 
                    notificationService.onPaymentFailed("{\"paymentId\": \"payment-" + iteration + "\"}"));
        }
    }
}
