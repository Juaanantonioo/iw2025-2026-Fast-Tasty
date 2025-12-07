package com.fastfoodmanager.repository;

import com.fastfoodmanager.domain.Allergen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AllergenRepository extends JpaRepository<Allergen, Long> {
    Optional<Allergen> findByName(String name);
}
