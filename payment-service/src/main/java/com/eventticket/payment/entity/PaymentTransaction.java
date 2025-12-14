package com.eventticket.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {
      @Id
      @GeneratedValue(strategy = GenerationType.UUID)
      private String id;

      @Column(nullable = false)
      private String userId;

      @Column(nullable = false)
      private String eventId;

      @Column(nullable = false)
      private double amount;

      @Column(nullable = false)
      private String status; // PENDING, CONFIRMED, FAILED, CANCELLED

      @Column(nullable = false)
      private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.

      @Column
      private String transactionId; // From payment gateway

      @Column(nullable = false)
      private LocalDateTime createdAt;

      @Column(nullable = false)
      private LocalDateTime updatedAt;

      @PrePersist
      protected void onCreate() {
            createdAt = LocalDateTime.now();
            updatedAt = LocalDateTime.now();
      }

      @PreUpdate
      protected void onUpdate() {
            updatedAt = LocalDateTime.now();
      }
}
