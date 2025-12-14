package com.eventticket.eventbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seats", uniqueConstraints = @UniqueConstraint(columnNames = { "event_id", "row", "col" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private String id;

     @Column(name = "event_id", nullable = false)
     private String eventId;

     @Column(nullable = false)
     private String row;

     @Column(nullable = false)
     private int col;

     @Column(nullable = false)
     private String status; // AVAILABLE, BLOCKED, SOLD

     @Column(columnDefinition = "TEXT")
     private String heldBy;

     @Column
     private Long heldUntil;

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
