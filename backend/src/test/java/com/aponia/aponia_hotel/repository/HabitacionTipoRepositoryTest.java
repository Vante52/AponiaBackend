package com.aponia.aponia_hotel.repository;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class HabitacionTipoRepositoryTest {

    @Autowired
    private HabitacionTipoRepository habitacionTipoRepository;

    private HabitacionTipo baseTipo;

    @BeforeEach
    void setUp() {
        baseTipo = crearHabitacion("Suite Base", "Habitación de prueba base", 2, new BigDecimal("200000"), true);
    }

    private HabitacionTipo crearHabitacion(String nombre, String descripcion, int aforo, BigDecimal precio, boolean activa) {
        HabitacionTipo tipo = new HabitacionTipo();
        tipo.setId(UUID.randomUUID().toString());
        tipo.setNombre(nombre);
        tipo.setDescripcion(descripcion);
        tipo.setAforoMaximo(aforo);
        tipo.setPrecioPorNoche(precio);
        tipo.setActiva(activa);
        return tipo;
    }

    // === CRUD ===

    @Test
    void HabitacionTipoRepository_save_HabitacionTipo() {
        HabitacionTipo guardado = habitacionTipoRepository.save(baseTipo);

        assertThat(guardado).isNotNull();
        assertThat(guardado.getId()).isEqualTo(baseTipo.getId());
        assertThat(guardado.getNombre()).isEqualTo("Suite Base");

        Optional<HabitacionTipo> encontrado = habitacionTipoRepository.findById(baseTipo.getId());
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getDescripcion()).contains("prueba base");
    }

    @Test
    void HabitacionTipoRepository_read_HabitacionTipo() {
        // Arrange
        habitacionTipoRepository.save(baseTipo);

        // Act
        Optional<HabitacionTipo> encontrado = habitacionTipoRepository.findById(baseTipo.getId());
        List<HabitacionTipo> todos = habitacionTipoRepository.findAll();

        // Assert
        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getNombre()).isEqualTo("Suite Base");
        assertThat(todos).isNotEmpty();
        assertThat(todos).extracting(HabitacionTipo::getNombre).contains("Suite Base");
    }

    @Test
    void HabitacionTipoRepository_update_HabitacionTipo() {
        HabitacionTipo guardado = habitacionTipoRepository.save(baseTipo);

        guardado.setDescripcion("Habitación con balcón y vista al jardín");
        guardado.setPrecioPorNoche(new BigDecimal("300000"));
        HabitacionTipo actualizado = habitacionTipoRepository.save(guardado);

        assertThat(actualizado.getDescripcion()).contains("balcón");
        assertThat(actualizado.getPrecioPorNoche()).isEqualByComparingTo("300000");
    }

    @Test
    void HabitacionTipoRepository_delete_HabitacionTipo() {
        HabitacionTipo guardado = habitacionTipoRepository.save(baseTipo);

        habitacionTipoRepository.deleteById(guardado.getId());

        Optional<HabitacionTipo> eliminado = habitacionTipoRepository.findById(guardado.getId());
        assertThat(eliminado).isEmpty();
    }

    // === Consultas personalizadas ===

    @Test
    void HabitacionTipoRepository_findByActivaIsTrue_retornaSoloActivas() {
        HabitacionTipo activa = crearHabitacion("Suite Activa", "Activa", 3, new BigDecimal("200000"), true);
        HabitacionTipo inactiva = crearHabitacion("Suite Inactiva", "Inactiva", 2, new BigDecimal("150000"), false);

        habitacionTipoRepository.save(activa);
        habitacionTipoRepository.save(inactiva);

        List<HabitacionTipo> activas = habitacionTipoRepository.findByActivaIsTrue();

        assertThat(activas)
                .isNotEmpty()
                .extracting(HabitacionTipo::getActiva)
                .containsOnly(true);
    }

    @Test
    void HabitacionTipoRepository_existsByNombre_devuelveTrueSiExiste() {
        habitacionTipoRepository.save(baseTipo);

        boolean existe = habitacionTipoRepository.existsByNombre("Suite Base");

        assertThat(existe).isTrue();
    }
}
