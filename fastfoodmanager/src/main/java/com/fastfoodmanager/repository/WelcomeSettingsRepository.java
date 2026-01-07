package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.WelcomeSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WelcomeSettingsRepository extends JpaRepository<WelcomeSettings, Long> {
}
