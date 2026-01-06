package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Allergen;
import com.fastfoodmanager.repository.AllergenRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AllergenService {

    private final AllergenRepository allergenRepo;

    public AllergenService(AllergenRepository allergenRepo) {
        this.allergenRepo = allergenRepo;
    }

    // Obtener todos los alérgenos
    public List<Allergen> findAll() {
        return allergenRepo.findAll();
    }

    // Guardar o actualizar un alérgeno
    public Allergen save(Allergen allergen) {
        return allergenRepo.save(allergen);
    }

    // Buscar por ID
    public Optional<Allergen> findById(Long id) {
        return allergenRepo.findById(id);
    }

    // Eliminar alérgeno
    public void delete(Allergen allergen) {
        allergenRepo.delete(allergen);
    }
}
