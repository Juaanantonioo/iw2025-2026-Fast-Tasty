package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.CashClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CashClosureRepository extends JpaRepository<CashClosure, Long> {

    List<CashClosure> findByBusinessDate(LocalDate businessDate);

    List<CashClosure> findByClosedAtBetween(LocalDateTime from, LocalDateTime to);
}
