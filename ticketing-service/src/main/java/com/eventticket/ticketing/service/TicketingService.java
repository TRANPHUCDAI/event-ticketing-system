package com.eventticket.ticketing.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.eventticket.common.dto.TicketDto;
import com.eventticket.ticketing.entity.Ticket;
import com.eventticket.ticketing.repository.TicketRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TicketingService {
     private static final Logger logger = LoggerFactory.getLogger(TicketingService.class);
     private final TicketRepository ticketRepository;

     public TicketingService(TicketRepository ticketRepository) {
          this.ticketRepository = ticketRepository;
     }

     /**
      * Listen to payment-confirmed events from Kafka
      * Khi Payment Service publish PaymentConfirmed event,
      * Ticketing Service sẽ nhận được và tạo ticket
      */
     @KafkaListener(topics = "payment-confirmed", groupId = "ticketing-service")
     public void onPaymentConfirmed(String message) {
          logger.info("Received PaymentConfirmed event: {}", message);
          // Parse message and create tickets
          // Format: {"paymentId": "xxx", "userId": "xxx", "eventId": "xxx", "amount":
          // 100.0}
     }

     public TicketDto createTicket(String eventId, String seatId, String userId, String paymentId) {
          logger.info("Creating ticket: eventId={}, seatId={}, userId={}, paymentId={}",
                    eventId, seatId, userId, paymentId);

          // Generate unique QR code
          String ticketQrCode = generateQrCode(eventId, seatId, userId);

          // Generate QR image
          byte[] qrImage = generateQrImage(ticketQrCode);

          Ticket ticket = Ticket.builder()
                    .eventId(eventId)
                    .seatId(seatId)
                    .userId(userId)
                    .paymentId(paymentId)
                    .qrCode(ticketQrCode)
                    .qrCodeImage(qrImage)
                    .status("ACTIVE")
                    .build();

          Ticket savedTicket = ticketRepository.save(ticket);
          logger.info("Ticket created successfully: ticketId={}, qrCode={}", savedTicket.getId(), ticketQrCode);

          return mapToDto(savedTicket);
     }

     public TicketDto getTicket(String ticketId) {
          Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));
          return mapToDto(ticket);
     }

     public List<TicketDto> getUserTickets(String userId) {
          return ticketRepository.findByUserId(userId)
                    .stream()
                    .map(this::mapToDto)
                    .toList();
     }

     public List<TicketDto> getEventTickets(String eventId) {
          return ticketRepository.findByEventId(eventId)
                    .stream()
                    .map(this::mapToDto)
                    .toList();
     }

     /**
      * Check-in: người dùng quét QR code khi vào sự kiện
      */
     public TicketDto checkIn(String ticketId) {
          logger.info("Check-in ticket: ticketId={}", ticketId);

          Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

          if (!"ACTIVE".equals(ticket.getStatus())) {
               throw new RuntimeException("Ticket is not active: " + ticket.getStatus());
          }

          ticket.setStatus("USED");
          ticket.setCheckedInAt(java.time.LocalDateTime.now());
          Ticket updated = ticketRepository.save(ticket);

          logger.info("Ticket check-in successful: ticketId={}", ticketId);
          return mapToDto(updated);
     }

     /**
      * Generate QR code data (format:
      * TICKET:ticketId:eventId:seatId:userId:timestamp)
      */
     private String generateQrCode(String eventId, String seatId, String userId) {
          long timestamp = System.currentTimeMillis();
          return String.format("TICKET:%s:%s:%s:%s:%d",
                    UUID.randomUUID().toString().substring(0, 8),
                    eventId.substring(0, 8),
                    seatId.substring(0, 8),
                    userId.substring(0, 8),
                    timestamp);
     }

     /**
      * Generate QR code image (PNG)
      * Using ZXing library
      */
     private byte[] generateQrImage(String qrCode) {
          try {
               QRCodeWriter qrCodeWriter = new QRCodeWriter();
               BitMatrix bitMatrix = qrCodeWriter.encode(qrCode, BarcodeFormat.QR_CODE, 300, 300);

               ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
               MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
               return outputStream.toByteArray();
          } catch (Exception e) {
               logger.error("Error generating QR code image", e);
               throw new RuntimeException("Failed to generate QR code image", e);
          }
     }

     private TicketDto mapToDto(Ticket ticket) {
          return TicketDto.builder()
                    .id(ticket.getId())
                    .eventId(ticket.getEventId())
                    .seatId(ticket.getSeatId())
                    .userId(ticket.getUserId())
                    .qrCode(ticket.getQrCode())
                    .status(ticket.getStatus())
                    .createdAt(ticket.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();
     }
}
