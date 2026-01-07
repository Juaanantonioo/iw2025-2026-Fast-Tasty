package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Order;
import com.fastfoodmanager.domain.OrderItem;
import com.fastfoodmanager.domain.OrderType;
import com.fastfoodmanager.domain.MenuSnapshot;
import com.fastfoodmanager.domain.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        log.info("EmailService inicializado. MailSender: {}", (mailSender != null ? "OK" : "NULL"));
    }

    // ========= MÉTODOS PARA RESERVAS =========

    public void enviarConfirmacionReserva(Booking reserva) {
        log.info("=== INTENTANDO ENVIAR EMAIL DE RESERVA ===");
        log.info("Destinatario: {}", reserva.getEmail());

        try {
            if (reserva.getEmail() == null || reserva.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Email del destinatario está vacío");
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(reserva.getEmail().trim());
            message.setSubject("Confirmación de Reserva - FastTasty");
            message.setText(construirContenidoEmailReserva(reserva));
            message.setFrom("noreply@fasttasty.com");

            log.debug("Configuración email de reserva:");
            log.debug("- To: {}", message.getTo());
            log.debug("- Subject: {}", message.getSubject());
            log.debug("- From: {}", message.getFrom());

            log.info("Enviando email de reserva...");
            mailSender.send(message);
            log.info("✅ EMAIL DE RESERVA ENVIADO EXITOSAMENTE a {}", reserva.getEmail());

        } catch (Exception e) {
            log.error("❌ FALLO AL ENVIAR EMAIL DE RESERVA:");
            log.error("Error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());

            if (e.getMessage().contains("535")) {
                log.error("PROBLEMA: Credenciales incorrectas de Gmail");
                log.error("SOLUCIÓN: Usa contraseña de aplicación, no tu contraseña normal");
            } else if (e.getMessage().contains("Could not connect to SMTP host")) {
                log.error("PROBLEMA: No se puede conectar a Gmail");
                log.error("SOLUCIÓN: Verifica firewall/antivirus. Prueba puerto 465");
            }

            throw new RuntimeException("Error al enviar email de reserva: " + e.getMessage(), e);
        }
    }

    private String construirContenidoEmailReserva(Booking reserva) {
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

    // ========= MÉTODOS PARA PEDIDOS =========

    public void enviarTicketPedido(Order pedido, String emailDestinatario) {
        log.info("=== INTENTANDO ENVIAR EMAIL DE PEDIDO ===");
        log.info("Enviando ticket del pedido #{} a {}", pedido.getId(), emailDestinatario);

        try {
            if (emailDestinatario == null || emailDestinatario.trim().isEmpty()) {
                throw new IllegalArgumentException("Email del destinatario está vacío");
            }

            // Validar que el pedido tenga ID (esté guardado en BD)
            if (pedido.getId() == null) {
                log.warn("⚠️ El pedido aún no tiene ID asignado (no se ha guardado en BD). No se enviará email.");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDestinatario.trim());
            message.setSubject("Ticket de Pedido #" + pedido.getId() + " - FastTasty");
            message.setText(construirContenidoTicketPedido(pedido));
            message.setFrom("noreply@fasttasty.com");

            log.debug("Configuración email de pedido:");
            log.debug("- To: {}", message.getTo());
            log.debug("- Subject: {}", message.getSubject());
            log.debug("- From: {}", message.getFrom());

            mailSender.send(message);
            log.info("✅ EMAIL DE PEDIDO ENVIADO EXITOSAMENTE a {}", emailDestinatario);

        } catch (Exception e) {
            log.error("❌ FALLO AL ENVIAR EMAIL DE PEDIDO:");
            log.error("Error: {}", e.getClass().getName());
            log.error("Mensaje: {}", e.getMessage());
            throw new RuntimeException("Error al enviar ticket por email: " + e.getMessage(), e);
        }
    }

    private String construirContenidoTicketPedido(Order pedido) {
        StringBuilder contenido = new StringBuilder();

        contenido.append("=".repeat(50)).append("\n");
        contenido.append("              FASTTASTY - TICKET DE PEDIDO\n");
        contenido.append("=".repeat(50)).append("\n\n");

        contenido.append("NÚMERO DE PEDIDO: ").append(pedido.getId()).append("\n");

        LocalDateTime fechaCreacion = pedido.getCreatedAt();
        if (fechaCreacion != null) {
            contenido.append("FECHA Y HORA: ").append(fechaCreacion.format(DATE_FORMATTER)).append("\n");
        } else {
            contenido.append("FECHA Y HORA: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        }

        contenido.append("CLIENTE: ").append(pedido.getCustomer().getUsername()).append("\n");
        contenido.append("EMAIL: ").append(pedido.getCustomer().getEmail()).append("\n");
        contenido.append("TIPO DE PEDIDO: ").append(
                pedido.getOrderType() == OrderType.PICKUP ? "RECOGER EN LOCAL" : "A DOMICILIO"
        ).append("\n");

        if (pedido.getDeliveryAddress() != null && !pedido.getDeliveryAddress().isEmpty()) {
            contenido.append("DIRECCIÓN DE ENTREGA: ").append(pedido.getDeliveryAddress()).append("\n");
        }

        contenido.append("\n");
        contenido.append("-".repeat(50)).append("\n");
        contenido.append("DETALLE DEL PEDIDO:\n");
        contenido.append("-".repeat(50)).append("\n");

        List<OrderItem> items = pedido.getItems();
        for (OrderItem item : items) {

            if (item.getProduct() != null) {
                // PRODUCTO INDIVIDUAL
                contenido.append(String.format(
                        "%-30s %3d x €%7.2f = €%7.2f%n",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ));

                if (item.getProduct().getIngredients() != null && !item.getProduct().getIngredients().isEmpty()) {
                    contenido.append("    Ingredientes:\n");
                    for (var ing : item.getProduct().getIngredients()) {
                        contenido.append(String.format(
                                "      - %-20s Cantidad: %s%s%n",
                                ing.getName(),
                                (int) ing.getQuantity(),
                                ing.isCustomizable() ? " (personalizable)" : ""
                        ));
                    }
                }

            } else if (item.getMenu() != null) {
                // MENÚ
                MenuSnapshot m = item.getMenu();

                contenido.append(String.format(
                        "%-30s %3d x €%7.2f = €%7.2f%n",
                        m.getName() + " (Menú)",
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ));

                // Mostrar categorías del menú
                if (!m.getMainProducts().isEmpty())
                    contenido.append("    Main (x").append(m.getMainQuantity()).append("): ")
                            .append(String.join(", ", m.getMainProducts())).append("\n");

                if (!m.getSideProducts().isEmpty())
                    contenido.append("    Side (x").append(m.getSideQuantity()).append("): ")
                            .append(String.join(", ", m.getSideProducts())).append("\n");

                if (!m.getDrinkProducts().isEmpty())
                    contenido.append("    Drink (x").append(m.getDrinkQuantity()).append("): ")
                            .append(String.join(", ", m.getDrinkProducts())).append("\n");

                if (!m.getSecondaryProducts().isEmpty())
                    contenido.append("    Secondary (x").append(m.getSecondaryQuantity()).append("): ")
                            .append(String.join(", ", m.getSecondaryProducts())).append("\n");

                if (!m.getDessertProducts().isEmpty())
                    contenido.append("    Dessert (x").append(m.getDessertQuantity()).append("): ")
                            .append(String.join(", ", m.getDessertProducts())).append("\n");
            }

            contenido.append("\n");

            // 🔥 INGREDIENTES DEL SNAPSHOT
            if (item.isProduct()
                    && item.getProduct().getIngredients() != null
                    && !item.getProduct().getIngredients().isEmpty()) {

                contenido.append("    Ingredientes:\n");

                for (var ing : item.getProduct().getIngredients()) {
                    contenido.append(String.format(
                            "      - %-20s Cantidad: %s%s%n",
                            ing.getName(),
                            (int) ing.getQuantity(),
                            ing.isCustomizable() ? " (personalizable)" : ""
                    ));
                }
            }

            contenido.append("\n");
        }

        contenido.append("-".repeat(50)).append("\n");
        contenido.append(String.format("SUBTOTAL: €%.2f%n", pedido.getTotal()));
        contenido.append(String.format("IVA (10%%): €%.2f%n", pedido.getTotal() * 0.10));
        contenido.append(String.format("TOTAL: €%.2f%n", pedido.getTotal() * 1.10));
        contenido.append("=".repeat(50)).append("\n\n");

        contenido.append("INFORMACIÓN IMPORTANTE:\n");
        contenido.append("-".repeat(50)).append("\n");

        if (pedido.getOrderType() == OrderType.PICKUP) {
            contenido.append("🕒 Tu pedido estará listo en aproximadamente 20-30 minutos.\n");
            contenido.append("📍 Dirección del local: Calle Comida Rápida 123, Madrid\n");
            contenido.append("📞 Teléfono: +34 91 123 45 67\n");
        } else {
            contenido.append("🕒 Tu pedido será entregado en aproximadamente 40-50 minutos.\n");
            contenido.append("📞 Si necesitas contactar con el repartidor: +34 91 987 65 43\n");
        }

        contenido.append("\n");
        contenido.append("¡Gracias por confiar en FastTasty!\n");
        contenido.append("Este es un ticket generado automáticamente.\n");

        return contenido.toString();
    }

    public void enviarConfirmacionEntrega(Order pedido) {
        log.info("=== INTENTANDO ENVIAR EMAIL DE ENTREGA/RECOGIDA ===");

        try {
            if (pedido == null) {
                log.error("❌ Pedido es NULL en enviarConfirmacionEntrega");
                return;
            }

            if (pedido.getCustomer() == null) {
                log.error("❌ Pedido #{} no tiene customer asociado", pedido.getId());
                return;
            }

            String email = pedido.getCustomer().getEmail();
            log.info("Pedido #{}, customer={}, email={}",
                    pedido.getId(),
                    pedido.getCustomer().getUsername(),
                    email);

            if (email == null || email.isBlank()) {
                log.warn("⚠️ Pedido #{}: email del cliente vacío o null. No se envía confirmación.", pedido.getId());
                return;
            }

            boolean esDelivery = pedido.getOrderType() == OrderType.DELIVERY;
            log.info("Tipo de pedido: {}, esDelivery={}", pedido.getOrderType(), esDelivery);

            // Forzar carga de items (por si acaso)
            if (pedido.getItems() != null) {
                log.info("Pedido #{} tiene {} items", pedido.getId(), pedido.getItems().size());
            } else {
                log.warn("Pedido #{} tiene items = null", pedido.getId());
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);

            message.setSubject(
                    esDelivery
                            ? "Tu pedido #" + pedido.getId() + " ha sido ENTREGADO ✔"
                            : "Tu pedido #" + pedido.getId() + " ha sido RECOGIDO ✔"
            );

            message.setFrom("noreply@fasttasty.com");

            String cuerpo = construirContenidoConfirmacionFinal(pedido, esDelivery);
            log.debug("Cuerpo de email de entrega/recogida:\n{}", cuerpo);
            message.setText(cuerpo);

            log.info("Enviando email de confirmación de entrega/recogida para pedido #{}...", pedido.getId());
            mailSender.send(message);
            log.info("✅ Email de confirmación enviado a {}", email);

        } catch (Exception e) {
            log.error("❌ Error enviando email de entrega/recogida para pedido #{}",
                    (pedido != null ? pedido.getId() : null), e);
        }
    }

    private String construirContenidoConfirmacionFinal(Order pedido, boolean esDelivery) {
        StringBuilder c = new StringBuilder();

        c.append("=".repeat(50)).append("\n");
        c.append("        FASTTASTY - CONFIRMACIÓN DE ")
                .append(esDelivery ? "ENTREGA" : "RECOGIDA")
                .append("\n");
        c.append("=".repeat(50)).append("\n\n");

        c.append("Hola ").append(pedido.getCustomer().getUsername()).append(",\n\n");

        if (esDelivery) {
            c.append("Tu pedido #").append(pedido.getId()).append(" ha sido ENTREGADO correctamente.\n");
            c.append("Esperamos que disfrutes tu comida 🍔🍟\n\n");
        } else {
            c.append("Tu pedido #").append(pedido.getId()).append(" ha sido RECOGIDO en nuestro local.\n");
            c.append("¡Gracias por visitarnos! 😄\n\n");
        }

        c.append("DETALLE DEL PEDIDO:\n");
        c.append("-".repeat(50)).append("\n");

        for (OrderItem item : pedido.getItems()) {
            if (item.getProduct() != null) {
                // PRODUCTO INDIVIDUAL
                c.append(String.format("%s x%d - €%.2f\n",
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getSubtotal()
                ));

                if (item.isProduct()
                        && item.getProduct().getIngredients() != null
                        && !item.getProduct().getIngredients().isEmpty()) {

                    c.append("   Ingredientes:\n");
                    for (var ing : item.getProduct().getIngredients()) {
                        c.append(String.format(
                                "     - %s: %d%s\n",
                                ing.getName(),
                                (int) ing.getQuantity(),
                                ing.isCustomizable() ? " (personalizable)" : ""
                        ));
                    }
                }

            } else if (item.getMenu() != null) {
                // MENÚ
                MenuSnapshot m = item.getMenu();

                c.append(String.format("%s (Menú) x%d - €%.2f\n",
                        m.getName(),
                        item.getQuantity(),
                        item.getSubtotal()
                ));

                if (!m.getMainProducts().isEmpty())
                    c.append("   Main (x").append(m.getMainQuantity()).append("): ")
                            .append(String.join(", ", m.getMainProducts())).append("\n");

                if (!m.getSideProducts().isEmpty())
                    c.append("   Side (x").append(m.getSideQuantity()).append("): ")
                            .append(String.join(", ", m.getSideProducts())).append("\n");

                if (!m.getDrinkProducts().isEmpty())
                    c.append("   Drink (x").append(m.getDrinkQuantity()).append("): ")
                            .append(String.join(", ", m.getDrinkProducts())).append("\n");

                if (!m.getSecondaryProducts().isEmpty())
                    c.append("   Secondary (x").append(m.getSecondaryQuantity()).append("): ")
                            .append(String.join(", ", m.getSecondaryProducts())).append("\n");

                if (!m.getDessertProducts().isEmpty())
                    c.append("   Dessert (x").append(m.getDessertQuantity()).append("): ")
                            .append(String.join(", ", m.getDessertProducts())).append("\n");
            }

            c.append("\n");

            if (item.isProduct()
                    && item.getProduct() != null
                    && item.getProduct().getIngredients() != null
                    && !item.getProduct().getIngredients().isEmpty()) {

                c.append("   Ingredientes:\n");
                for (var ing : item.getProduct().getIngredients()) {
                    c.append(String.format(
                            "     - %s: %d%s\n",
                            ing.getName(),
                            (int) ing.getQuantity(),
                            ing.isCustomizable() ? " (personalizable)" : ""
                    ));
                }
            }

            c.append("\n");
        }

        c.append("-".repeat(50)).append("\n");
        c.append(String.format("TOTAL PAGADO: €%.2f\n", pedido.getTotal()));
        c.append("=".repeat(50)).append("\n\n");

        // ⭐ Enlace a valoración
        c.append("VALORA TU EXPERIENCIA:\n");
        c.append("Haz clic aquí para dejarnos tu valoración:\n");
        c.append("➡ https://fasttasty.com/rating?order=").append(pedido.getId()).append("\n\n");

        if (esDelivery) {
            c.append("Gracias por confiar en FastTasty ❤️\n");
            c.append("¡Esperamos que disfrutes tu comida!\n");
        } else {
            c.append("Gracias por tu visita ❤️\n");
            c.append("¡Esperamos verte pronto de nuevo!\n");
        }

        return c.toString();
    }
}