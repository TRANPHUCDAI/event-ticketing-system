package com.eventticket.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

     public static void main(String[] args) {
          SpringApplication.run(ApiGatewayApplication.class, args);
     }

     @Bean
     public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
          return builder.routes()
                    .route("event-booking-service", r -> r
                              .path("/api/events/**", "/api/seats/**")
                              .uri("http://localhost:8001"))
                    .route("payment-service", r -> r
                              .path("/api/payments/**")
                              .uri("http://localhost:8003"))
                    .route("ticketing-service", r -> r
                              .path("/api/tickets/**")
                              .uri("http://localhost:8004"))
                    .route("notification-analytics-service", r -> r
                              .path("/api/notifications/**", "/api/reports/**")
                              .uri("http://localhost:8005"))
                    .build();
     }
}
