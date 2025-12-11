package com.fastfoodmanager.services;

import com.fastfoodmanager.models.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("EmailService inicializado. MailSender: {}", (mailSender != null ? "OK" : "NULL"));
    }

    public void enviarConfirmacionReserva(Booking reserva) {
        log.info("=== INTENTANDO ENVIAR EMAIL ===");
        log.info("Destinatario: {}", reserva.getEmail());

        try {
            // Verificar que el email no esté vacío
            if (reserva.getEmail() == null || reserva.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email del destinatario está vacío");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reserva.getEmail().trim());
            message.setSubject("Confirmación de Reserva - FastTasty");
            message.setText(construirContenidoEmail(reserva));
            message.setFrom("noreply@fasttasty.com");

            log.debug("Configuración email:");
            log.debug("- To: {}", message.getTo());
            log.debug("- Subject: {}", message.getSubject());
            log.debug("- From: {}", message.getFrom());

            // INTENTAR ENVIAR
            log.info("Enviando email...");
            mailSender.send(message);
            log.info("✅ EMAIL ENVIADO EXITOSAMENTE a {}", reserva.getEmail());

        } catch (Exception e) {
            log.error("❌ FALLO AL ENVIAR EMAIL:");
            log.error("Error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());

            // Mostrar más detalles para Gmail específicamente
            if (e.getMessage().contains("535")) {
                log.error("PROBLEMA: Credenciales incorrectas de Gmail");
                log.error("SOLUCIÓN: Usa contraseña de aplicación, no tu contraseña normal");
            } else if (e.getMessage().contains("Could not connect to SMTP host")) {
                log.error("PROBLEMA: No se puede conectar a Gmail");
                log.error("SOLUCIÓN: Verifica firewall/antivirus. Prueba puerto 465");
            }

            throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
        }
    }

    private String construirContenidoEmail(Booking reserva) {
        StringBuilder contenido = new StringBuilder();

        contenido.append("¡Hola ").append(reserva.getNombre()).append("!\n\n");
        contenido.append("Gracias por reservar en FastTasty. Aquí tienes los detalles de tu reserva:\n\n");

        contenido.append("📅 FECHA: ").append(reserva.getFecha()).append("\n");
        contenido.append("⏰ HORA: ").append(reserva.getHora()).append("\n");
        contenido.append("👥 PERSONAS: ").append(reserva.getPersonas()).append("\n");
        contenido.append("📞 TELÉFONO: ").append(reserva.getTelefono()).append("\n");
        contenido.append("📧 EMAIL: ").append(reserva.getEmail()).append("\n");

        if (reserva.getComentarios() != null && !reserva.getComentarios().trim().isEmpty()) {
            contenido.append("💬 COMENTARIOS: ").append(reserva.getComentarios()).append("\n");
        }

        contenido.append("\n");
        contenido.append("📍 Ubicación: Calle Ejemplo 123, Sevilla\n");
        contenido.append("📞 Teléfono del restaurante: +34 123 456 789\n\n");

        contenido.append("Política de cancelación:\n");
        contenido.append("Puedes cancelar o modificar tu reserva con al menos 2 horas de antelación.\n\n");

        contenido.append("¡Esperamos verte pronto!\n");
        contenido.append("El equipo de FastTasty 🍔");

        return contenido.toString();
    }
}