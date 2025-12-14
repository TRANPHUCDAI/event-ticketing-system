package com.eventticket.eventbooking.seat.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.eventticket.common.dto.SeatDto;
import com.eventticket.common.exception.SeatAlreadyHeldException;
import com.eventticket.eventbooking.seat.entity.SeatReservation;
import com.eventticket.eventbooking.seat.repository.SeatReservationRepository;

@ExtendWith(MockitoExtension.class)
class SeatAllocationServiceTest {
    @Mock
    private SeatReservationRepository seatReservationRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeatAllocationService seatAllocationService;

    private SeatReservation testReservation;

    @BeforeEach
    void setUp() {
        testReservation = SeatReservation.builder()
                .id("reservation-123")
                .eventId("event-456")
                .seatId("seat-789")
                .userId("user-001")
                .status("HELD")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Test
    void testHoldSeat_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(true);
        when(seatReservationRepository.save(any(SeatReservation.class))).thenReturn(testReservation);

        SeatDto result = seatAllocationService.holdSeat("event-456", "seat-789", "user-001");

        assertNotNull(result);
        assertEquals("seat-789", result.getId());
        assertEquals("BLOCKED", result.getStatus());
        assertEquals("user-001", result.getHeldBy());
        verify(seatReservationRepository, times(1)).save(any(SeatReservation.class));
    }

    @Test
    void testHoldSeat_SeatAlreadyHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any())).thenReturn(false);
        when(valueOperations.get(anyString())).thenReturn("user-002");

        assertThrows(SeatAlreadyHeldException.class, 
                () -> seatAllocationService.holdSeat("event-456", "seat-789", "user-001"));
    }

    @Test
    void testReleaseSeat_Success() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.of(testReservation));
        when(seatReservationRepository.save(any(SeatReservation.class))).thenReturn(testReservation);

        assertDoesNotThrow(() -> seatAllocationService.releaseSeat("event-456", "seat-789", "user-001"));

        verify(redisTemplate, times(1)).delete(anyString());
        verify(seatReservationRepository, times(1)).save(any(SeatReservation.class));
    }

    @Test
    void testReleaseSeat_NotFound() {
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.empty());

        // Should not throw exception, just release from Redis
        assertDoesNotThrow(() -> seatAllocationService.releaseSeat("event-456", "seat-789", "user-001"));

        verify(redisTemplate, times(1)).delete(anyString());
    }

    @Test
    void testConfirmSeat_Success() {
        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.of(testReservation));
        when(seatReservationRepository.save(any(SeatReservation.class))).thenReturn(testReservation);

        assertDoesNotThrow(() -> seatAllocationService.confirmSeat("event-456", "seat-789", "user-001"));

        verify(seatReservationRepository, times(1)).save(any(SeatReservation.class));
    }

    @Test
    void testConfirmSeat_NotFound() {
        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
                () -> seatAllocationService.confirmSeat("event-456", "seat-789", "user-001"));
    }

    @Test
    void testConfirmSeat_WrongUser() {
        SeatReservation otherUserReservation = SeatReservation.builder()
                .id("reservation-123")
                .eventId("event-456")
                .seatId("seat-789")
                .userId("user-002")
                .status("HELD")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.of(otherUserReservation));

        assertThrows(RuntimeException.class, 
                () -> seatAllocationService.confirmSeat("event-456", "seat-789", "user-001"));
    }

    @Test
    void testGetSeatStatus_Held() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_lock:event-456:seat-789")).thenReturn("user-001");
        when(redisTemplate.getExpire("seat_lock:event-456:seat-789")).thenReturn(300L);

        SeatDto result = seatAllocationService.getSeatStatus("event-456", "seat-789");

        assertNotNull(result);
        assertEquals("seat-789", result.getId());
        assertEquals("BLOCKED", result.getStatus());
        assertEquals("user-001", result.getHeldBy());
    }

    @Test
    void testGetSeatStatus_Available() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("seat_lock:event-456:seat-789")).thenReturn(null);

        SeatDto result = seatAllocationService.getSeatStatus("event-456", "seat-789");

        assertNotNull(result);
        assertEquals("seat-789", result.getId());
        assertEquals("AVAILABLE", result.getStatus());
        assertNull(result.getHeldBy());
    }

    @Test
    void testCleanupExpiredReservations() {
        when(seatReservationRepository.findByStatusAndExpiresAtBefore(eq("HELD"), any(LocalDateTime.class)))
                .thenReturn(java.util.List.of(testReservation));
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(seatReservationRepository.findByEventIdAndSeatIdAndStatus("event-456", "seat-789", "HELD"))
                .thenReturn(Optional.of(testReservation));
        when(seatReservationRepository.save(any(SeatReservation.class))).thenReturn(testReservation);

        assertDoesNotThrow(() -> seatAllocationService.cleanupExpiredReservations());

        verify(seatReservationRepository, times(1)).findByStatusAndExpiresAtBefore(eq("HELD"), any(LocalDateTime.class));
    }
}
