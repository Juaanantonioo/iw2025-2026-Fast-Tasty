package com.fastfoodmanager.service;

import com.fastfoodmanager.domain.Menu;
import com.fastfoodmanager.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenusService {

    private final MenuRepository menuRepository;

    public MenusService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    // Obtener todos los menús
    public List<Menu> findAll() {
        return menuRepository.findAll();
    }

    // Obtener solo menús activos
    public List<Menu> findActiveMenus() {
        return menuRepository.findAll()
                .stream()
                .filter(Menu::isActive)
                .toList();
    }

    // Alias opcional si lo quieres usar en otros sitios
    public List<Menu> findAllActive() {
        return findActiveMenus();
    }

    // Buscar por ID
    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    // Guardar o actualizar menú
    public Menu save(Menu menu) {
        return menuRepository.save(menu);
    }

    // Eliminar menú
    public void delete(Long id) {
        menuRepository.deleteById(id);
    }

    // Comprobar si existe
    public boolean exists(Long id) {
        return menuRepository.existsById(id);
    }
}
