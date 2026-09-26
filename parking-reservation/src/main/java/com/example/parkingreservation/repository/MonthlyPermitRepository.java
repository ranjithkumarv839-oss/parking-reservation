package com.example.parkingreservation.repository;

import com.example.parkingreservation.model.MonthlyPermit;
import com.example.parkingreservation.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyPermitRepository extends JpaRepository<MonthlyPermit, Long> {
    List<MonthlyPermit> findByUserOrderByExpiryDateDesc(User user);
    Optional<MonthlyPermit> findFirstByUserAndStatusAndExpiryDateAfter(User user, String status, LocalDate date);
}
