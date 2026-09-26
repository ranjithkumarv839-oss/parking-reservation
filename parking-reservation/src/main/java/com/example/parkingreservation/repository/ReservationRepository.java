package com.example.parkingreservation.repository;

import com.example.parkingreservation.model.Reservation;
import com.example.parkingreservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserOrderByStartTimeDesc(User user);
}