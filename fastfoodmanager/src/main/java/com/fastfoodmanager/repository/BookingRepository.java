package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByFechaAndHora(LocalDate fecha, LocalTime hora);
}