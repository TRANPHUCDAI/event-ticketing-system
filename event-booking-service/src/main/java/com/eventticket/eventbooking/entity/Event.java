package com.eventticket.eventbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
     @Id
     @GeneratedValue(strategy = GenerationType.UUID)
     private String id;

     @Column(nullable = false)
     private String name;

     @Column(nullable = false)
     private String venueName;

     @Column(nullable = false)
     private LocalDateTime startTime;

     @Column(nullable = false)
     private LocalDateTime endTime;

     @Column(columnDefinition = "TEXT")
     private String description;

     @Column(nullable = false)
     private int totalSeats;

     @Column(nullable = false)
     private int availableSeats;

     @Column(nullable = false)
     private int soldSeats;

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
