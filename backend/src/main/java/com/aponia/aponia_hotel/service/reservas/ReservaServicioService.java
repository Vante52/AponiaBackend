package com.aponia.aponia_hotel.service.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;

public interface ReservaServicioService {
    /**
     * Lista todas las reservas de servicios
     */
    List<ReservaServicio> listar();

    /**
     * Lista las reservas de servicios por reserva
     */
    List<ReservaServicio> findByReservaId(String reservaId);

    /**
     * Lista las reservas de servicios por servicio y fecha
     */
    List<ReservaServicio> findByServicioIdAndFecha(String servicioId, LocalDate fecha);
    /**
     * Crea una nueva reserva de servicio
     */
    void crear(ReservaServicio reservaServicio);

    /**
     * Obtiene una reserva de servicio por su ID
     */
    Optional<ReservaServicio> obtener(String id);

    /**
     * Actualiza una reserva de servicio existente
     */
    void actualizar(ReservaServicio reservaServicio);

    /**
     * Elimina una reserva de servicio por su ID
     */
    void eliminar(String id);

}