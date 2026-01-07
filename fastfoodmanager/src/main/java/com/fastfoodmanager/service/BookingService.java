package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Booking;
import com.fastfoodmanager.repository.BookingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class BookingService {

    private final EmailService emailService;
    private final BookingRepository bookingRepository;

    // Horario permitido para reservas (según tu requisito)
    private static final LocalTime OPENING = LocalTime.of(12, 0);
    private static final LocalTime CLOSING = LocalTime.of(23, 30);

    public BookingService(EmailService emailService, BookingRepository bookingRepository) {
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void procesarReserva(Booking reserva) {
        try {
            // 0) Validaciones básicas de nulidad
            if (reserva == null || reserva.getFecha() == null || reserva.getHora() == null) {
                throw new IllegalArgumentException("Fecha y hora son obligatorias.");
            }

            // 1) No permitir reservas fuera del horario (12:00 → 23:30)
            if (reserva.getHora().isBefore(OPENING) || reserva.getHora().isAfter(CLOSING)) {
                throw new IllegalArgumentException("La hora de reserva debe estar entre 12:00 y 23:30.");
            }

            // 2) No permitir reservas en el pasado (fecha/hora anteriores al momento actual)
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime requested = LocalDateTime.of(reserva.getFecha(), reserva.getHora());

            if (reserva.getFecha().isBefore(today)) {
                throw new IllegalArgumentException("No se permiten reservas en fechas pasadas.");
            }
            if (requested.isBefore(now)) {
                throw new IllegalArgumentException("No se permiten reservas en una hora que ya ha pasado.");
            }

            // 3) Validación de duplicado (misma fecha + misma hora)
            if (bookingRepository.existsByFechaAndHora(reserva.getFecha(), reserva.getHora())) {
                throw new IllegalArgumentException("Ya existe una reserva para esa fecha y hora.");
            }

            // 4) Persistencia
            bookingRepository.save(reserva);

            // 5) Email
            emailService.enviarConfirmacionReserva(reserva);

        } catch (DataIntegrityViolationException e) {
            // Si dos usuarios intentan reservar a la vez, la BD puede “ganar” con constraint unique
            throw new IllegalArgumentException("Ya existe una reserva para esa fecha y hora.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la reserva: " + e.getMessage(), e);
        }
    }
}