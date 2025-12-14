package com.eventticket.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
     private String id;
     private String name;
     private String venueName;
     private long startTime;
     private long endTime;
     private String description;
     private int totalSeats;
     private int availableSeats;
     private int soldSeats;
}
