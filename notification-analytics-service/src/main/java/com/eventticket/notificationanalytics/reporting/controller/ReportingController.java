package com.eventticket.notificationanalytics.reporting.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.notificationanalytics.reporting.dto.EventReportDto;
import com.eventticket.notificationanalytics.reporting.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {
     private final ReportingService reportingService;

     @GetMapping("/events/{eventId}")
     public ResponseEntity<ApiResponse<EventReportDto>> getEventReport(@PathVariable String eventId) {
          EventReportDto report = reportingService.getEventReport(eventId);
          return ResponseEntity.ok(ApiResponse.ok(report));
     }

     @GetMapping("/events")
     public ResponseEntity<ApiResponse<EventReportDto[]>> getAllEventReports() {
          EventReportDto[] reports = reportingService.getAllEventReports();
          return ResponseEntity.ok(ApiResponse.ok(reports));
     }
}
