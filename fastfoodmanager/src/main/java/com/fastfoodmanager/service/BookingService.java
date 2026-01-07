package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Booking;
import com.fastfoodmanager.repository.BookingRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class BookingService {

    private final EmailService emailService;
    private final BookingRepository bookingRepository;

    public BookingService(EmailService emailService, BookingRepository bookingRepository) {
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void procesarReserva(Booking reserva) {
        try {
            // 1) Validación “rápida” en aplicación
            if (bookingRepository.existsByFechaAndHora(reserva.getFecha(), reserva.getHora())) {
                throw new IllegalArgumentException("Ya existe una reserva para esa fecha y hora.");
            }

            // 2) Persistencia
            bookingRepository.save(reserva);

            // 3) Confirmación por email
            emailService.enviarConfirmacionReserva(reserva);

        } catch (DataIntegrityViolationException e) {
            // Por si dos usuarios reservan a la vez: manda el error “bonito”
            throw new IllegalArgumentException("Ya existe una reserva para esa fecha y hora.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la reserva: " + e.getMessage(), e);
        }
    }
}