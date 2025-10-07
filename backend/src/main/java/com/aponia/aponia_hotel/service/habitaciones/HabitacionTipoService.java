package com.aponia.aponia_hotel.service.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import java.util.List;
import java.util.Optional;

public interface HabitacionTipoService {
    /**
     * Lista todos los tipos de habitación
     */
    List<HabitacionTipo> listar();

    /**
     * Lista los tipos de habitación activos
     */
    List<HabitacionTipo> listarActivos();

    /**
     * Crea un nuevo tipo de habitación
     */
    HabitacionTipo crear(HabitacionTipo tipo);

    /**
     * Obtiene un tipo de habitación por su ID
     */
    Optional<HabitacionTipo> obtener(String id);

    /**
     * Actualiza un tipo de habitación existente
     */
    HabitacionTipo actualizar(HabitacionTipo tipo);

    /**
     * Elimina un tipo de habitación por su ID
     */
    void eliminar(String id);
}