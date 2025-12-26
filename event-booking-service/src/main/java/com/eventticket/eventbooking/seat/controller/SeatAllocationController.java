package com.eventticket.eventbooking.seat.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.common.dto.SeatDto;
import com.eventticket.eventbooking.seat.service.SeatAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
public class SeatAllocationController {
      private static final Logger logger = LoggerFactory.getLogger(SeatAllocationController.class);
      private final SeatAllocationService seatAllocationService;

      public SeatAllocationController(SeatAllocationService seatAllocationService) {
            this.seatAllocationService = seatAllocationService;
      }

      @PostMapping("/hold")
      public ResponseEntity<ApiResponse<SeatDto>> holdSeat(
                  @RequestParam("eventId") String eventId,
                  @RequestParam("seatId") String seatId,
                  @RequestParam("userId") String userId) {
            logger.info("Received hold seat request: eventId={}, seatId={}, userId={}", eventId, seatId, userId);
            try {
                  SeatDto seat = seatAllocationService.holdSeat(eventId, seatId, userId);
                  logger.info("Hold seat successful");
                  return ResponseEntity
                                  .status(HttpStatus.CREATED)
                                  .body(ApiResponse.ok(seat, "Seat held successfully for 5 minutes"));
            } catch (Exception e) {
                  logger.error("Hold seat failed with exception", e);
                  return ResponseEntity
                                  .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                  .body(ApiResponse.error(e.getMessage(), "HOLD_SEAT_ERROR"));
            }
      }

      @PostMapping("/release")
      public ResponseEntity<ApiResponse<Void>> releaseSeat(
                  @RequestParam("eventId") String eventId,
                  @RequestParam("seatId") String seatId,
                  @RequestParam("userId") String userId) {
            seatAllocationService.releaseSeat(eventId, seatId, userId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Seat released successfully"));
      }

      @PostMapping("/confirm")
      public ResponseEntity<ApiResponse<Void>> confirmSeat(
                  @RequestParam("eventId") String eventId,
                  @RequestParam("seatId") String seatId,
                  @RequestParam("userId") String userId) {
            seatAllocationService.confirmSeat(eventId, seatId, userId);
            return ResponseEntity.ok(ApiResponse.ok(null, "Seat confirmed successfully"));
      }

      @GetMapping("/status")
      public ResponseEntity<ApiResponse<SeatDto>> getSeatStatus(
                  @RequestParam("eventId") String eventId,
                  @RequestParam("seatId") String seatId) {
            SeatDto seat = seatAllocationService.getSeatStatus(eventId, seatId);
            return ResponseEntity.ok(ApiResponse.ok(seat));
      }
}
