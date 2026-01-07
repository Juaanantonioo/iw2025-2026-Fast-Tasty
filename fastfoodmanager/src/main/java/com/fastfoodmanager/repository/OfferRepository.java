package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Todas las ofertas activas (sin mirar fechas)
    List<Offer> findByActiveTrue();

    // Ofertas activas y dentro de su rango de fechas
    @Query("""
        SELECT o FROM Offer o
        WHERE o.active = true
        AND (o.startDate IS NULL OR o.startDate <= :now)
        AND (o.endDate IS NULL OR o.endDate >= :now)
    """)
    List<Offer> findValidOffers(LocalDateTime now);
}
