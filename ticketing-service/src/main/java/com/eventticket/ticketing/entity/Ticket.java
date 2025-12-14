package com.eventticket.ticketing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private String id;

     @Column(nullable = false)
     private String eventId;

     @Column(nullable = false)
     private String seatId;

     @Column(nullable = false)
     private String userId;

     @Column(nullable = false)
     private String paymentId;

     @Column(nullable = false)
     private String qrCode;

     @Column(columnDefinition = "BYTEA")
     private byte[] qrCodeImage;

     @Column(nullable = false)
     private String status; // ACTIVE, USED, CANCELLED

     @Column(nullable = false)
     private LocalDateTime createdAt;

     @Column(nullable = false)
     private LocalDateTime updatedAt;

     @Column
     private LocalDateTime checkedInAt;

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
