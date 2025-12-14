package com.eventticket.ticketing.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventticket.common.dto.TicketDto;
import com.eventticket.ticketing.entity.Ticket;
import com.eventticket.ticketing.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketingServiceTest {
    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketingService ticketingService;

    private Ticket testTicket;

    @BeforeEach
    void setUp() {
        testTicket = new Ticket();
        testTicket.setId("ticket-123");
        testTicket.setEventId("event-456");
        testTicket.setSeatId("seat-789");
        testTicket.setUserId("user-001");
        testTicket.setPaymentId("payment-555");
        testTicket.setQrCode("QR_CODE_123");
        testTicket.setQrCodeImage(new byte[]{1, 2, 3});
        testTicket.setStatus("ACTIVE");
        testTicket.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateTicket_Success() {
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        TicketDto result = ticketingService.createTicket("event-456", "seat-789", "user-001", "payment-555");

        assertNotNull(result);
        assertEquals("ticket-123", result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getQrCode());
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testGetTicket_Success() {
        when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(testTicket));

        TicketDto result = ticketingService.getTicket("ticket-123");

        assertNotNull(result);
        assertEquals("ticket-123", result.getId());
        assertEquals("event-456", result.getEventId());
        verify(ticketRepository, times(1)).findById("ticket-123");
    }

    @Test
    void testGetTicket_NotFound() {
        when(ticketRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ticketingService.getTicket("nonexistent"));
    }

    @Test
    void testGetUserTickets_Success() {
        when(ticketRepository.findByUserId("user-001"))
                .thenReturn(java.util.List.of(testTicket));

        var results = ticketingService.getUserTickets("user-001");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("user-001", results.get(0).getUserId());
        verify(ticketRepository, times(1)).findByUserId("user-001");
    }

    @Test
    void testGetEventTickets_Success() {
        when(ticketRepository.findByEventId("event-456"))
                .thenReturn(java.util.List.of(testTicket));

        var results = ticketingService.getEventTickets("event-456");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("event-456", results.get(0).getEventId());
        verify(ticketRepository, times(1)).findByEventId("event-456");
    }

    @Test
    void testCheckIn_Success() {
        when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        TicketDto result = ticketingService.checkIn("ticket-123");

        assertNotNull(result);
        assertEquals("ticket-123", result.getId());
        verify(ticketRepository, times(1)).findById("ticket-123");
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void testCheckIn_InvalidStatus() {
        Ticket usedTicket = new Ticket();
        usedTicket.setId("ticket-123");
        usedTicket.setStatus("USED");

        when(ticketRepository.findById("ticket-123")).thenReturn(Optional.of(usedTicket));

        assertThrows(RuntimeException.class, () -> ticketingService.checkIn("ticket-123"));
    }

    @Test
    void testOnPaymentConfirmed_ValidMessage() {
        assertDoesNotThrow(() -> ticketingService.onPaymentConfirmed(
                "{\"paymentId\": \"pay-123\", \"userId\": \"user-001\", \"eventId\": \"event-456\", \"amount\": 100.0}"
        ));
    }
}
