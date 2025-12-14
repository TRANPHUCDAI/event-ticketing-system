package com.eventticket.eventbooking.service;

import com.eventticket.common.dto.EventDto;
import com.eventticket.common.exception.ResourceNotFoundException;
import com.eventticket.eventbooking.entity.Event;
import com.eventticket.eventbooking.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {
     private final EventRepository eventRepository;

     public EventDto createEvent(EventDto eventDto) {
          Event event = Event.builder()
                    .name(eventDto.getName())
                    .venueName(eventDto.getVenueName())
                    .description(eventDto.getDescription())
                    .startTime(LocalDateTime.now().plusDays(1))
                    .endTime(LocalDateTime.now().plusDays(1).plusHours(3))
                    .totalSeats(eventDto.getTotalSeats())
                    .availableSeats(eventDto.getTotalSeats())
                    .soldSeats(0)
                    .build();

          Event savedEvent = eventRepository.save(event);
          return mapToDto(savedEvent);
     }

     public EventDto getEvent(String eventId) {
          Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
          return mapToDto(event);
     }

     public List<EventDto> getAllEvents() {
          return eventRepository.findAll()
                    .stream()
                    .map(this::mapToDto)
                    .toList();
     }

     public EventDto updateEventSeatStatus(String eventId, int availableSeats, int soldSeats) {
          Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));

          event.setAvailableSeats(availableSeats);
          event.setSoldSeats(soldSeats);

          Event updatedEvent = eventRepository.save(event);
          return mapToDto(updatedEvent);
     }

     private EventDto mapToDto(Event event) {
          return EventDto.builder()
                    .id(event.getId())
                    .name(event.getName())
                    .venueName(event.getVenueName())
                    .description(event.getDescription())
                    .startTime(event.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .endTime(event.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .totalSeats(event.getTotalSeats())
                    .availableSeats(event.getAvailableSeats())
                    .soldSeats(event.getSoldSeats())
                    .build();
     }
}
