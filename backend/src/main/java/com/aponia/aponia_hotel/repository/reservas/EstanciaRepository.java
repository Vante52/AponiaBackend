package com.aponia.aponia_hotel.repository.reservas;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aponia.aponia_hotel.entities.reservas.Estancia;

public interface EstanciaRepository extends JpaRepository<Estancia, String> {

    List<Estancia> findByReservaId(String reservaId);

    @Query("SELECT COUNT(DISTINCT e.habitacionAsignada) FROM Estancia e WHERE e.tipoHabitacion.id = :tipoHabitacionId AND e.reserva.estado = 'CONFIRMADA' AND (e.entrada < :checkOut AND e.salida > :checkIn)")
    long contarHabitacionesOcupadas(@Param("tipoHabitacionId") String tipoHabitacionId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

    @Query("SELECT e FROM Estancia e WHERE e.habitacionAsignada.id = :habitacionId AND e.reserva.estado = 'CONFIRMADA' AND (e.entrada < :checkOut AND e.salida > :checkIn)")
    List<Estancia> findOverlappingStays(@Param("habitacionId") String habitacionId, @Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut);

    @Query("SELECT e FROM Estancia e WHERE e.tipoHabitacion.id = :tipoHabitacionId AND e.reserva.estado = 'CONFIRMADA' AND e.entrada = :fecha")
    List<Estancia> findCheckinsByTipoHabitacionAndFecha(@Param("tipoHabitacionId") String tipoHabitacionId, @Param("fecha") LocalDate fecha);

    @Query("SELECT e FROM Estancia e WHERE e.tipoHabitacion.id = :tipoHabitacionId AND e.reserva.estado = 'CONFIRMADA' AND e.salida = :fecha")
    List<Estancia> findCheckoutsByTipoHabitacionAndFecha(@Param("tipoHabitacionId") String tipoHabitacionId, @Param("fecha") LocalDate fecha);

    @Query("SELECT e FROM Estancia e WHERE e.habitacionAsignada.id = :habitacionId AND e.reserva.estado = 'CONFIRMADA'")
    List<Estancia> findByHabitacionIdAndReservaActiva(@Param("habitacionId") String habitacionId);
}
