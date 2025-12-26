package com.eventticket.eventbooking.seat.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.common.dto.SeatDto;
import com.eventticket.eventbooking.seat.service.SeatAllocationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
public class SeatAllocationController {
      private final SeatAllocationService seatAllocationService;

      public SeatAllocationController(SeatAllocationService seatAllocationService) {
            this.seatAllocationService = seatAllocationService;
      }

      @PostMapping("/hold")
      public ResponseEntity<ApiResponse<SeatDto>> holdSeat(
                  @RequestParam String eventId,
                  @RequestParam String seatId,
                  @RequestParam String userId) {
            SeatDto seat = seatAllocationService.holdSeat(eventId, seatId, userId);
            return ResponseEntity
                            .status(HttpStatus.CREATED)
                            .body(ApiResponse.ok(seat, "Seat held successfully for 5 minutes"));
      }

      @PostMapping("/release")
      public ResponseEntity<ApiResponse<Void>> releaseSeat(
                  @RequestParam String eventId,
                  @RequestParam String seatId,
                  @RequestParam String userId) {
            seatAllocationService.releaseSeat(eventId, seatId, userId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Seat released successfully"));
      }

      @PostMapping("/confirm")
      public ResponseEntity<ApiResponse<Void>> confirmSeat(
                  @RequestParam String eventId,
                  @RequestParam String seatId,
                  @RequestParam String userId) {
            seatAllocationService.confirmSeat(eventId, seatId, userId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Seat confirmed successfully"));
      }

      @GetMapping("/status")
      public ResponseEntity<ApiResponse<SeatDto>> getSeatStatus(
                  @RequestParam String eventId,
                  @RequestParam String seatId) {
            SeatDto seat = seatAllocationService.getSeatStatus(eventId, seatId);
            return ResponseEntity.ok(ApiResponse.ok(seat));
      }
}
