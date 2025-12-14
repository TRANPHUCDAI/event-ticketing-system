package com.eventticket.eventbooking.seat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatReservation {
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
      private String status; // HELD, CONFIRMED, RELEASED

      @Column(nullable = false)
      private LocalDateTime heldAt;

      @Column(nullable = false)
      private LocalDateTime expiresAt;

      @Column
      private LocalDateTime confirmedAt;

      @PrePersist
      protected void onCreate() {
            heldAt = LocalDateTime.now();
      }
}
