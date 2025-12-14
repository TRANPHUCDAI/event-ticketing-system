package com.eventticket.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eventticket.payment.entity.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {
      Optional<PaymentTransaction> findByTransactionId(String transactionId);
     Optional<PaymentTransaction> findByIdAndStatus(String id, String status);
}
