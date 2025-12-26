package com.eventticket.ticketing.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.common.dto.TicketDto;
import com.eventticket.ticketing.service.TicketingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketingController {
     private final TicketingService ticketingService;

     public TicketingController(TicketingService ticketingService) {
          this.ticketingService = ticketingService;
     }

     @PostMapping
     public ResponseEntity<ApiResponse<TicketDto>> createTicket(
               @RequestParam String eventId,
               @RequestParam String seatId,
               @RequestParam String userId,
               @RequestParam String paymentId) {

          TicketDto ticket = ticketingService.createTicket(eventId, seatId, userId, paymentId);
          return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(ticket, "Ticket created successfully"));
     }

     @GetMapping("/{ticketId}")
     public ResponseEntity<ApiResponse<TicketDto>> getTicket(@PathVariable String ticketId) {
          TicketDto ticket = ticketingService.getTicket(ticketId);
          return ResponseEntity.ok(ApiResponse.ok(ticket));
     }

     @GetMapping("/user/{userId}")
     public ResponseEntity<ApiResponse<List<TicketDto>>> getUserTickets(@PathVariable String userId) {
          List<TicketDto> tickets = ticketingService.getUserTickets(userId);
          return ResponseEntity.ok(ApiResponse.ok(tickets));
     }

     @GetMapping("/event/{eventId}")
     public ResponseEntity<ApiResponse<List<TicketDto>>> getEventTickets(@PathVariable String eventId) {
          List<TicketDto> tickets = ticketingService.getEventTickets(eventId);
          return ResponseEntity.ok(ApiResponse.ok(tickets));
     }

     @PostMapping("/{ticketId}/checkin")
     public ResponseEntity<ApiResponse<TicketDto>> checkIn(@PathVariable String ticketId) {
          TicketDto ticket = ticketingService.checkIn(ticketId);
          return ResponseEntity.ok(ApiResponse.ok(ticket, "Check-in successful"));
     }
}
