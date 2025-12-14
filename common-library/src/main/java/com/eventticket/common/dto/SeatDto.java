package com.eventticket.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatDto {
     private String id;
     private String eventId;
     private String row;
     private int col;
     private String status; // AVAILABLE, BLOCKED, SOLD
     private String heldBy;
     private long heldUntil;
}
