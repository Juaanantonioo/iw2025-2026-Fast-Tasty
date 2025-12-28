package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.CashClosure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CashClosureRepository extends JpaRepository<CashClosure, Long> {

    Optional<CashClosure> findTopByOrderByClosedAtDesc();

    List<CashClosure> findByBusinessDate(LocalDate businessDate);

    List<CashClosure> findByClosedAtBetween(LocalDateTime start, LocalDateTime end);
}
