package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.MenuCardSettings;
import com.fastfoodmanager.repository.MenuCardSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class MenuCardSettingsService {

    private final MenuCardSettingsRepository repo;

    public MenuCardSettingsService(MenuCardSettingsRepository repo) {
        this.repo = repo;
    }

    public MenuCardSettings get() {
        return repo.findById(1L).orElseGet(() -> {
            MenuCardSettings s = new MenuCardSettings();
            s.setName("Menús");
            return repo.save(s);
        });
    }

    public MenuCardSettings save(MenuCardSettings settings) {
        return repo.save(settings);
    }
}
