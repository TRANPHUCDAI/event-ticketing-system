package com.eventticket.notificationanalytics.reporting.service;

import com.eventticket.notificationanalytics.reporting.dto.EventReportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportingService {
     private final WebClient webClient;

     public EventReportDto getEventReport(String eventId) {
          log.info("Generating report for event: {}", eventId);

          // Call Event Service to get event details`
          // Call Ticketing Service to get sold tickets
          // Call Payment Service to get revenue

          // For now, return mock data
          return EventReportDto.builder()
                    .eventId(eventId)
                    .eventName("Sample Event")
                    .totalSeats(100)
                    .soldSeats(75)
                    .availableSeats(25)
                    .totalRevenue(7500.0)
                    .occupancyRate(75.0)
                    .build();
     }

     public EventReportDto[] getAllEventReports() {
          log.info("Generating reports for all events");

          // Query all services and aggregate data
          return new EventReportDto[] {
                    EventReportDto.builder()
                              .eventId("event-1")
                              .eventName("Concert A")
                              .totalSeats(500)
                              .soldSeats(450)
                              .availableSeats(50)
                              .totalRevenue(45000.0)
                              .occupancyRate(90.0)
                              .build(),
                    EventReportDto.builder()
                              .eventId("event-2")
                              .eventName("Conference B")
                              .totalSeats(200)
                              .soldSeats(150)
                              .availableSeats(50)
                              .totalRevenue(15000.0)
                              .occupancyRate(75.0)
                              .build()
          };
     }
}
