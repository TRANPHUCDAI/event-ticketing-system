package com.eventticket.notificationanalytics.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
     private final JavaMailSender mailSender;

     @KafkaListener(topics = "ticket-created", groupId = "notification-service")
     public void onTicketCreated(String message) {
          log.info("Received TicketCreated event: {}", message);
          // Parse message and send email
          // Format: {"ticketId": "xxx", "userId": "xxx", "qrCode": "xxx"}
          sendTicketEmail(message);
     }

     @KafkaListener(topics = "payment-failed", groupId = "notification-service")
     public void onPaymentFailed(String message) {
          log.info("Received PaymentFailed event: {}", message);
          // Send notification about failed payment
          sendPaymentFailureEmail(message);
     }

     private void sendTicketEmail(String ticketInfo) {
          try {
               log.info("Sending ticket email...");
               // In production, parse the message and get user email
               // Then send email with QR code attachment

               // Mock implementation
               SimpleMailMessage message = new SimpleMailMessage();
               message.setTo("user@example.com");
               message.setSubject("Your Event Ticket - QR Code Attached");
               message.setText("Your ticket has been confirmed. Please find the QR code attached to this email.");

               // mailSender.send(message);
               log.info("Ticket email sent successfully (mocked)");
          } catch (Exception e) {
               log.error("Failed to send ticket email", e);
          }
     }

     private void sendPaymentFailureEmail(String paymentInfo) {
          try {
               log.info("Sending payment failure email...");
               // Mock implementation
               log.info("Payment failure email sent successfully (mocked)");
          } catch (Exception e) {
               log.error("Failed to send payment failure email", e);
          }
     }
}
