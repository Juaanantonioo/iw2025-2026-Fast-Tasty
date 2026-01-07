package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.WelcomeSettings;
import com.fastfoodmanager.repository.WelcomeSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WelcomeSettingsService {

    private final WelcomeSettingsRepository repo;

    public WelcomeSettingsService(WelcomeSettingsRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public WelcomeSettings get() {
        return repo.findAll().stream().findFirst().orElseGet(() -> {
            WelcomeSettings ws = new WelcomeSettings();
            ws.setSiteTitle("FastTasty | Bienvenido");
            ws.setSiteSubtitle("Comida deliciosa, rápida y al alcance de un clic.");
            ws.setSiteDomain("FastTasty");
            ws.setAddress("Calle Ejemplo 123, Sevilla");
            ws.setGoogleMapsUrl("https://www.google.com/maps?q=Calle+Ejemplo+123+Sevilla&output=embed");
            ws.setScheduleText("""
                    Lunes - Jueves: 12:30 – 16:00 / 20:00 – 23:30
                    Viernes: 12:30 – 16:00 / 20:00 – 00:00
                    Sábado: 13:00 – 00:00
                    Domingo: 13:00 – 23:30
                    """);
            return repo.save(ws);
        });
    }

    @Transactional
    public WelcomeSettings save(WelcomeSettings ws) {
        return repo.save(ws);
    }
}
