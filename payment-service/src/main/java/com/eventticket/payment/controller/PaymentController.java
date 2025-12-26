package com.eventticket.payment.controller;

import com.eventticket.common.dto.ApiResponse;
import com.eventticket.payment.entity.PaymentTransaction;
import com.eventticket.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
     private final PaymentService paymentService;

     public PaymentController(PaymentService paymentService) {
          this.paymentService = paymentService;
     }

     @PostMapping
     public ResponseEntity<ApiResponse<String>> createPayment(
               @RequestParam("userId") String userId,
               @RequestParam("eventId") String eventId,
               @RequestParam("amount") double amount,
               @RequestParam("paymentMethod") String paymentMethod) {

          String paymentId = paymentService.createPayment(userId, eventId, amount, paymentMethod);
          return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.ok(paymentId, "Payment created successfully"));
     }

     @PostMapping("/{paymentId}/confirm")
     public ResponseEntity<ApiResponse<Void>> confirmPayment(
               @PathVariable("paymentId") String paymentId,
               @RequestParam("transactionId") String transactionId) {

          paymentService.confirmPayment(paymentId, transactionId);
          return ResponseEntity.ok(ApiResponse.ok(null, "Payment confirmed successfully"));
     }

     @GetMapping("/{paymentId}")
     public ResponseEntity<ApiResponse<PaymentTransaction>> getPaymentStatus(@PathVariable("paymentId") String paymentId) {
          PaymentTransaction payment = paymentService.getPaymentStatus(paymentId);
          return ResponseEntity.ok(ApiResponse.ok(payment));
     }
}
