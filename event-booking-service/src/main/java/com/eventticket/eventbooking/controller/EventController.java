package com.eventticket.eventbooking.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.common.dto.EventDto;
import com.eventticket.eventbooking.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

     @PostMapping
     public ResponseEntity<ApiResponse<EventDto>> createEvent(@RequestBody EventDto eventDto) {
          EventDto createdEvent = eventService.createEvent(eventDto);
          return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(createdEvent, "Event created successfully"));
     }

     @GetMapping("/{eventId}")
     public ResponseEntity<ApiResponse<EventDto>> getEvent(@PathVariable String eventId) {
          EventDto event = eventService.getEvent(eventId);
          return ResponseEntity.ok(ApiResponse.ok(event));
     }

     @GetMapping
     public ResponseEntity<ApiResponse<List<EventDto>>> getAllEvents() {
          List<EventDto> events = eventService.getAllEvents();
          return ResponseEntity.ok(ApiResponse.ok(events));
     }
}
