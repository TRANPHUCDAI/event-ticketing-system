package com.eventticket.eventbooking.seat.repository;

import com.eventticket.eventbooking.seat.entity.SeatReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, String> {
      Optional<SeatReservation> findByEventIdAndSeatIdAndStatus(String eventId, String seatId, String status);

      List<SeatReservation> findByEventIdAndStatus(String eventId, String status);

      List<SeatReservation> findByUserIdAndStatus(String userId, String status);

      List<SeatReservation> findByStatusAndExpiresAtBefore(String status, java.time.LocalDateTime dateTime);
}
