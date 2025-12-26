package com.eventticket.eventbooking.seat.service;

import com.eventticket.common.dto.SeatDto;
import com.eventticket.common.exception.SeatAlreadyHeldException;
import com.eventticket.eventbooking.seat.entity.SeatReservation;
import com.eventticket.eventbooking.seat.repository.SeatReservationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
public class SeatAllocationService {
     private static final Logger logger = LoggerFactory.getLogger(SeatAllocationService.class);
     private final SeatReservationRepository reservationRepository;
     private final RedisTemplate<String, String> redisTemplate;

     public SeatAllocationService(SeatReservationRepository reservationRepository, RedisTemplate<String, String> redisTemplate) {
          this.reservationRepository = reservationRepository;
          this.redisTemplate = redisTemplate;
     }

     private static final long HOLD_DURATION_MINUTES = 5;
     private static final String SEAT_LOCK_PREFIX = "seat_lock:";

     /**
      * Hold seat với Redis distributed lock
      * Redis command: SET key value NX PX 300000
      * Nếu key đã tồn tại, operation sẽ fail
      */
     public SeatDto holdSeat(String eventId, String seatId, String userId) {
          logger.info("Attempting to hold seat: eventId={}, seatId={}, userId={}", eventId, seatId, userId);

          String lockKey = SEAT_LOCK_PREFIX + eventId + ":" + seatId;

          // Try to acquire lock with Redis NX (only set if not exists)
          Boolean lockAcquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, userId, Duration.ofMinutes(HOLD_DURATION_MINUTES));

          if (Boolean.FALSE.equals(lockAcquired)) {
               String currentHolder = redisTemplate.opsForValue().get(lockKey);
               logger.warn("Seat already held by: {}", currentHolder);
               throw new SeatAlreadyHeldException(seatId);
          }

          // Save reservation to database
          LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(HOLD_DURATION_MINUTES);
          SeatReservation reservation = SeatReservation.builder()
                    .eventId(eventId)
                    .seatId(seatId)
                    .userId(userId)
                    .status("HELD")
                    .expiresAt(expiresAt)
                    .build();

          SeatReservation saved = reservationRepository.save(reservation);
          logger.info("Seat held successfully: reservationId={}", saved.getId());

          return SeatDto.builder()
                    .id(seatId)
                    .eventId(eventId)
                    .status("BLOCKED")
                    .heldBy(userId)
                    .heldUntil(expiresAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();
     }

     /**
      * Release seat (người dùng hủy hoặc hết thời gian)
      */
     public void releaseSeat(String eventId, String seatId, String userId) {
          logger.info("Releasing seat: eventId={}, seatId={}, userId={}", eventId, seatId, userId);

          String lockKey = SEAT_LOCK_PREFIX + eventId + ":" + seatId;

          // Delete from Redis
          redisTemplate.delete(lockKey);

          // Update reservation status in database
          reservationRepository.findByEventIdAndSeatIdAndStatus(eventId, seatId, "HELD")
                    .ifPresent(reservation -> {
                         reservation.setStatus("RELEASED");
                         reservationRepository.save(reservation);
                    });

          logger.info("Seat released: eventId={}, seatId={}", eventId, seatId);
     }

     /**
      * Confirm seat ownership (sau khi thanh toán thành công)
      */
     public void confirmSeat(String eventId, String seatId, String userId) {
          logger.info("Confirming seat: eventId={}, seatId={}, userId={}", eventId, seatId, userId);

          SeatReservation reservation = reservationRepository
                    .findByEventIdAndSeatIdAndStatus(eventId, seatId, "HELD")
                    .orElseThrow(() -> new RuntimeException("Reservation not found for seat: " + seatId));

          if (!reservation.getUserId().equals(userId)) {
               throw new RuntimeException("User does not own this reservation");
          }

          reservation.setStatus("CONFIRMED");
          reservation.setConfirmedAt(LocalDateTime.now());
          reservationRepository.save(reservation);

          logger.info("Seat confirmed: eventId={}, seatId={}", eventId, seatId);
     }

     /**
      * Get current seat status
      */
     public SeatDto getSeatStatus(String eventId, String seatId) {
          String lockKey = SEAT_LOCK_PREFIX + eventId + ":" + seatId;
          String heldBy = redisTemplate.opsForValue().get(lockKey);

          if (heldBy != null) {
               Long ttl = redisTemplate.getExpire(lockKey);
               long heldUntil = System.currentTimeMillis() + (ttl != null ? ttl * 1000 : 0);

               return SeatDto.builder()
                         .id(seatId)
                         .eventId(eventId)
                         .status("BLOCKED")
                         .heldBy(heldBy)
                         .heldUntil(heldUntil)
                         .build();
          }

          return SeatDto.builder()
                    .id(seatId)
                    .eventId(eventId)
                    .status("AVAILABLE")
                    .build();
     }

     /**
      * Clean up expired reservations (background job)
      */
     public void cleanupExpiredReservations() {
          logger.info("Cleaning up expired reservations...");
          LocalDateTime now = LocalDateTime.now();
          var expiredReservations = reservationRepository.findByStatusAndExpiresAtBefore("HELD", now);

          for (SeatReservation reservation : expiredReservations) {
               releaseSeat(reservation.getEventId(), reservation.getSeatId(), reservation.getUserId());
          }

          logger.info("Cleanup completed. Expired reservations: {}", expiredReservations.size());
     }
}
