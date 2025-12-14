package com.eventticket.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {
     private String id;
     private String eventId;
     private String seatId;
     private String userId;
     private String qrCode;
     private String status;
     private long createdAt;
}
