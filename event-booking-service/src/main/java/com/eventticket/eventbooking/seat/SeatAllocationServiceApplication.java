package com.eventticket.eventbooking.seat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeatAllocationServiceApplication {
      public static void main(String[] args) {
            SpringApplication.run(SeatAllocationServiceApplication.class, args);
      }
}
