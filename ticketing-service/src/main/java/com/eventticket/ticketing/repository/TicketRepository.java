package com.eventticket.ticketing.repository;

import com.eventticket.ticketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {
     Optional<Ticket> findByQrCode(String qrCode);

     List<Ticket> findByUserId(String userId);

     List<Ticket> findByEventId(String eventId);

     List<Ticket> findByEventIdAndStatus(String eventId, String status);
}
