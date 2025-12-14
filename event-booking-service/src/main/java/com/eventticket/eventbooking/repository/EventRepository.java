package com.eventticket.eventbooking.repository;

import com.eventticket.eventbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
     Optional<Event> findByName(String name);
}
