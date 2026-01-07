package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.OfferCardSettings;
import com.fastfoodmanager.repository.OfferCardSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class OfferCardSettingsService {

    private final OfferCardSettingsRepository repo;

    public OfferCardSettingsService(OfferCardSettingsRepository repo) {
        this.repo = repo;
    }

    public OfferCardSettings get() {
        return repo.findAll().stream()
                .findFirst()
                .orElseGet(() -> repo.save(new OfferCardSettings()));
    }

    public OfferCardSettings save(OfferCardSettings settings) {
        return repo.save(settings);
    }
}
