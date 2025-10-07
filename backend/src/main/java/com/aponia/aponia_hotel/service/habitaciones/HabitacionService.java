package com.aponia.aponia_hotel.service.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import java.util.List;
import java.util.Optional;

public interface HabitacionService {
    /**
     * Lista todas las habitaciones
     */
    List<Habitacion> listar();

    /**
     * Lista todas las habitaciones activas
     */
    List<Habitacion> listarActivas();

    /**
     * Lista las habitaciones por tipo
     */
    List<Habitacion> listarPorTipo(String tipoId);

    /**
     * Crea una nueva habitación
     */
    Habitacion crear(Habitacion habitacion);

    /**
     * Obtiene una habitación por su ID
     */
    Optional<Habitacion> obtener(String id);

    /**
     * Actualiza una habitación existente
     */
    Habitacion actualizar(Habitacion habitacion);

    /**
     * Elimina una habitación por su ID
     */
    void eliminar(String id);

    // Métodos deprecados o no utilizados
    // @deprecated Usar listarPorTipo() en su lugar
    // List<Habitacion> findByTipoId(String tipoId);

    // @deprecated No implementado actualmente
    // List<Habitacion> findDisponiblesByTipoAndFechas(String tipoId, String checkIn, String checkOut);
}