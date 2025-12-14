package com.eventticket.notificationanalytics.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventReportDto {
     private String eventId;
     private String eventName;
     private int totalSeats;
     private int soldSeats;
     private int availableSeats;
     private double totalRevenue;
     private double occupancyRate;
}
