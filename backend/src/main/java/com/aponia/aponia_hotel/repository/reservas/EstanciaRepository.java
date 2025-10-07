package com.aponia.aponia_hotel.repository.reservas;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EstanciaRepository extends JpaRepository<Estancia, String> {
    List<Estancia> findByReservaId(String reservaId);

    @Query("SELECT COUNT(DISTINCT e.habitacionAsignada) FROM Estancia e " +
           "WHERE e.tipoHabitacion.id = :tipoHabitacionId " +
           "AND e.reserva.estado = 'CONFIRMADA' " +
           "AND ((e.checkIn <= :checkOut AND e.checkOut >= :checkIn))")
    long contarHabitacionesOcupadas(
        @Param("tipoHabitacionId") String tipoHabitacionId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut);

    @Query("SELECT e FROM Estancia e " +
           "WHERE e.habitacionAsignada.id = :habitacionId " +
           "AND e.reserva.estado = 'CONFIRMADA' " +
           "AND ((e.checkIn <= :checkOut AND e.checkOut >= :checkIn))")
    List<Estancia> findOverlappingStays(
        @Param("habitacionId") String habitacionId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut);

    @Query("SELECT e FROM Estancia e " +
           "WHERE e.tipoHabitacion.id = :tipoHabitacionId " +
           "AND e.reserva.estado = 'CONFIRMADA' " +
           "AND e.checkIn = :fecha")
    List<Estancia> findCheckinsByTipoHabitacionAndFecha(
        @Param("tipoHabitacionId") String tipoHabitacionId,
        @Param("fecha") LocalDate fecha);

    @Query("SELECT e FROM Estancia e " +
           "WHERE e.tipoHabitacion.id = :tipoHabitacionId " +
           "AND e.reserva.estado = 'CONFIRMADA' " +
           "AND e.checkOut = :fecha")
    List<Estancia> findCheckoutsByTipoHabitacionAndFecha(
        @Param("tipoHabitacionId") String tipoHabitacionId,
        @Param("fecha") LocalDate fecha);
}