package com.aponia.aponia_hotel.service.servicios;

import com.aponia.aponia_hotel.entities.servicios.ServicioDisponibilidad;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ServicioDisponibilidadService {
    /**
     * Lista todas las disponibilidades
     */
    List<ServicioDisponibilidad> listar();

    /**
     * Lista las disponibilidades de un servicio en una fecha con capacidad suficiente
     */
    List<ServicioDisponibilidad> listarDisponibles(String servicioId, LocalDate fecha, int capacidadRequerida);

    /**
     * Lista las disponibilidades de un servicio en un rango de fechas
     */
    List<ServicioDisponibilidad> listarPorRangoFechas(String servicioId, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Crea una nueva disponibilidad
     */
    ServicioDisponibilidad crear(ServicioDisponibilidad disponibilidad);

    /**
     * Obtiene una disponibilidad por su ID
     */
    Optional<ServicioDisponibilidad> obtener(String id);

    /**
     * Busca una disponibilidad específica por servicio, fecha y hora
     */
    Optional<ServicioDisponibilidad> buscarDisponibilidad(String servicioId, LocalDate fecha, LocalTime horaInicio);

    /**
     * Verifica si existe una disponibilidad para un servicio en una fecha y hora específicas
     */
    boolean existeDisponibilidad(String servicioId, LocalDate fecha, LocalTime horaInicio);

    /**
     * Actualiza una disponibilidad existente
     */
    ServicioDisponibilidad actualizar(ServicioDisponibilidad disponibilidad);

    /**
     * Elimina una disponibilidad por su ID
     */
    void eliminar(String id);
}