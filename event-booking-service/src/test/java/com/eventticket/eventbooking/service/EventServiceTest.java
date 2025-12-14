package com.eventticket.eventbooking.service;

import java.time.LocalDateTime;
import java.util.Optional;

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

import com.eventticket.common.dto.EventDto;
import com.eventticket.common.exception.ResourceNotFoundException;
import com.eventticket.eventbooking.entity.Event;
import com.eventticket.eventbooking.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private EventDto testEventDto;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId("event-123");
        testEvent.setName("Concert Night");
        testEvent.setVenueName("Grand Hall");
        testEvent.setDescription("Amazing concert");
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));
        testEvent.setTotalSeats(1000);
        testEvent.setAvailableSeats(1000);
        testEvent.setSoldSeats(0);

        testEventDto = new EventDto();
        testEventDto.setName("Concert Night");
        testEventDto.setVenueName("Grand Hall");
        testEventDto.setDescription("Amazing concert");
        testEventDto.setTotalSeats(1000);
    }

    @Test
    void testCreateEvent_Success() {
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventDto result = eventService.createEvent(testEventDto);

        assertNotNull(result);
        assertEquals("Concert Night", result.getName());
        assertEquals("Grand Hall", result.getVenueName());
        assertEquals(1000, result.getTotalSeats());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testGetEvent_Success() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(testEvent));

        EventDto result = eventService.getEvent("event-123");

        assertNotNull(result);
        assertEquals("event-123", result.getId());
        assertEquals("Concert Night", result.getName());
        verify(eventRepository, times(1)).findById("event-123");
    }

    @Test
    void testGetEvent_NotFound() {
        when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.getEvent("nonexistent"));
    }

    @Test
    void testGetAllEvents_Success() {
        when(eventRepository.findAll()).thenReturn(java.util.List.of(testEvent));

        var results = eventService.getAllEvents();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Concert Night", results.get(0).getName());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testUpdateEventSeatStatus_Success() {
        when(eventRepository.findById("event-123")).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventDto result = eventService.updateEventSeatStatus("event-123", 900, 100);

        assertNotNull(result);
        assertEquals(900, result.getAvailableSeats());
        assertEquals(100, result.getSoldSeats());
        verify(eventRepository, times(1)).findById("event-123");
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testUpdateEventSeatStatus_NotFound() {
        when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
                () -> eventService.updateEventSeatStatus("nonexistent", 900, 100));
    }

    @Test
    void testGetAllEvents_Empty() {
        when(eventRepository.findAll()).thenReturn(java.util.List.of());

        var results = eventService.getAllEvents();

        assertNotNull(results);
        assertEquals(0, results.size());
        verify(eventRepository, times(1)).findAll();
    }
}
