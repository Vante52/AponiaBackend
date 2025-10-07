package com.aponia.aponia_hotel.repository.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HabitacionTipoRepository extends JpaRepository<HabitacionTipo, String> {
    List<HabitacionTipo> findByActivaIsTrue();
    boolean existsByNombre(String nombre);
}