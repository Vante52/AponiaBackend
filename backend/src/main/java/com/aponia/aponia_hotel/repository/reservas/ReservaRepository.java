package com.aponia.aponia_hotel.repository.reservas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, String> {

        @Query("SELECT DISTINCT r FROM Reserva r "
                        + "LEFT JOIN FETCH r.estancias e "
                        + "LEFT JOIN FETCH e.tipoHabitacion "
                        + "LEFT JOIN FETCH e.habitacionAsignada "
                        + // Solo una colección
                        "WHERE r.cliente.id = :clienteId "
                        + "ORDER BY e.entrada DESC")
        List<Reserva> findByClienteId(@Param("clienteId") String clienteId);

        Optional<Reserva> findByCodigo(String codigo);

        boolean existsByCodigo(String codigo);

        List<Reserva> findByEstado(EstadoReserva estado);

        List<Reserva> findByClienteIdAndEstadoIn(String clienteId, Collection<EstadoReserva> estados);

        @Query("SELECT r FROM Reserva r WHERE r.estado = :estado AND r.fechaCreacion <= :fecha")
        List<Reserva> findReservasVencidas(@Param("estado") EstadoReserva estado, @Param("fecha") LocalDateTime fecha);

        @Query("SELECT r FROM Reserva r JOIN r.estancias e " +
                        "WHERE r.estado = :estado AND e.entrada = :fecha")
        List<Reserva> findReservasDelDia(@Param("estado") EstadoReserva estado,
                        @Param("fecha") LocalDate fecha);

        // En EstanciaRepository.java
        @Query("SELECT e FROM Estancia e "
                        + "JOIN FETCH e.reserva r "
                        + "JOIN FETCH r.cliente c "
                        + "LEFT JOIN FETCH c.clientePerfil "
                        + "WHERE e.habitacionAsignada.id = :habitacionId "
                        + "AND r.estado = 'CONFIRMADA' "
                        + "AND e.entrada <= CURRENT_DATE "
                        + "AND e.salida >= CURRENT_DATE "
                        + "ORDER BY e.entrada DESC")
        List<Estancia> findEstanciasActivasPorHabitacion(@Param("habitacionId") String habitacionId);
}
