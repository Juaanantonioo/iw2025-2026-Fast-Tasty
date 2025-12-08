package com.fastfoodmanager.services;

import com.fastfoodmanager.models.Booking;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final EmailService emailService;

    public BookingService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void procesarReserva(Booking reserva) {
        try {
            // Aquí podrías guardar la reserva en una base de datos si lo necesitas
            // reservaRepository.save(reserva);

            // Enviar email de confirmación
            emailService.enviarConfirmacionReserva(reserva);

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar la reserva: " + e.getMessage(), e);
        }
    }
}