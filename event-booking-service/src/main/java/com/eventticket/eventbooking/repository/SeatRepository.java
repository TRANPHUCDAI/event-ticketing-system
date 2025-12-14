package com.eventticket.eventbooking.repository;

import com.eventticket.eventbooking.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, String> {
      List<Seat> findByEventId(String eventId);

      Optional<Seat> findByEventIdAndRowAndCol(String eventId, String row, int col);

      List<Seat> findByEventIdAndStatus(String eventId, String status);
}
