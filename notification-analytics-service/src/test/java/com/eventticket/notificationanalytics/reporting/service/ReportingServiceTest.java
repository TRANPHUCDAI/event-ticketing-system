package com.eventticket.notificationanalytics.reporting.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventticket.notificationanalytics.reporting.dto.EventReportDto;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {
    @InjectMocks
    private ReportingService reportingService;

    @Test
    void testReportingServiceInitialized() {
        assertNotNull(reportingService);
    }

    @Test
    void testGetEventReport_Success() {
        EventReportDto report = reportingService.getEventReport("event-123");

        assertNotNull(report);
        assertEquals("event-123", report.getEventId());
        assertEquals("Sample Event", report.getEventName());
        assertEquals(100, report.getTotalSeats());
        assertEquals(75, report.getSoldSeats());
        assertEquals(25, report.getAvailableSeats());
        assertEquals(7500.0, report.getTotalRevenue());
        assertEquals(75.0, report.getOccupancyRate());
    }

    @Test
    void testGetEventReport_OccupancyCalculation() {
        EventReportDto report = reportingService.getEventReport("event-123");

        // Verify occupancy rate is calculated correctly
        double expectedOccupancy = (75.0 / 100.0) * 100;
        assertEquals(expectedOccupancy, report.getOccupancyRate());
    }

    @Test
    void testGetAllEventReports_Success() {
        EventReportDto[] reports = reportingService.getAllEventReports();

        assertNotNull(reports);
        assertEquals(2, reports.length);
    }

    @Test
    void testGetAllEventReports_FirstEvent() {
        EventReportDto[] reports = reportingService.getAllEventReports();

        EventReportDto firstReport = reports[0];
        assertEquals("event-1", firstReport.getEventId());
        assertEquals("Concert A", firstReport.getEventName());
        assertEquals(500, firstReport.getTotalSeats());
        assertEquals(450, firstReport.getSoldSeats());
        assertEquals(90.0, firstReport.getOccupancyRate());
        assertEquals(45000.0, firstReport.getTotalRevenue());
    }

    @Test
    void testGetAllEventReports_SecondEvent() {
        EventReportDto[] reports = reportingService.getAllEventReports();

        EventReportDto secondReport = reports[1];
        assertEquals("event-2", secondReport.getEventId());
        assertEquals("Conference B", secondReport.getEventName());
        assertEquals(200, secondReport.getTotalSeats());
        assertEquals(150, secondReport.getSoldSeats());
        assertEquals(75.0, secondReport.getOccupancyRate());
        assertEquals(15000.0, secondReport.getTotalRevenue());
    }

    @Test
    void testGetEventReport_AllFieldsPopulated() {
        EventReportDto report = reportingService.getEventReport("event-456");

        assertNotNull(report.getEventId());
        assertNotNull(report.getEventName());
        assertTrue(report.getTotalSeats() > 0);
        assertTrue(report.getSoldSeats() >= 0);
        assertTrue(report.getAvailableSeats() >= 0);
        assertTrue(report.getTotalRevenue() >= 0);
        assertTrue(report.getOccupancyRate() >= 0);
    }

    @Test
    void testGetAllEventReports_DataConsistency() {
        EventReportDto[] reports = reportingService.getAllEventReports();

        for (EventReportDto report : reports) {
            // Verify seat counts
            int totalCalculated = report.getSoldSeats() + report.getAvailableSeats();
            assertEquals(report.getTotalSeats(), totalCalculated);

            // Verify occupancy rate calculation
            double expectedOccupancy = (report.getSoldSeats() * 100.0) / report.getTotalSeats();
            assertEquals(expectedOccupancy, report.getOccupancyRate(), 0.01);
        }
    }
}
