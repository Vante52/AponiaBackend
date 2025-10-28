package com.aponia.aponia_hotel.service.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.reservas.Estancia;

public interface EstanciaService {

    /**
     * Lista todas las estancias
     */
    List<Estancia> listar();

    /**
     * Lista las estancias por reserva
     */
    List<Estancia> listarPorReserva(String reservaId);

    /**
     * Lista las estancias que tienen check-in en una fecha específica
     */
    List<Estancia> listarCheckinsDelDia(String tipoHabitacionId, LocalDate fecha);

    /**
     * Lista las estancias que tienen check-out en una fecha específica
     */
    List<Estancia> listarCheckoutsDelDia(String tipoHabitacionId, LocalDate fecha);

    /**
     * Verifica si hay habitaciones disponibles del tipo solicitado para las
     * fechas dadas
     */
    boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate checkIn, LocalDate checkOut, int numeroHuespedes);

    /**
     * Asigna una habitación disponible a la estancia
     */
    Estancia asignarHabitacion(String estanciaId);

    /**
     * Cuenta las habitaciones ocupadas de un tipo para un rango de fechas
     */
    long contarHabitacionesOcupadas(String tipoHabitacionId, LocalDate checkIn, LocalDate checkOut);

    /**
     * Crea una nueva estancia
     */
    Estancia crear(Estancia estancia);

    /**
     * Obtiene una estancia por su ID
     */
    Optional<Estancia> obtener(String id);

    /**
     * Actualiza una estancia existente
     */
    Estancia actualizar(Estancia estancia);

    /**
     * Elimina una estancia por su ID
     */
    void eliminar(String id);

    /**
     * Verifica si hay conflictos de fechas para una habitación
     */
    List<Estancia> buscarConflictosFechas(String habitacionId, LocalDate checkIn, LocalDate checkOut);

    List<Estancia> obtenerEstanciasActivasPorHabitacion(String habitacionId);

    void realizarCheckout(String estanciaId);
}
