package com.aponia.aponia_hotel.repository.habitaciones;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, String> {
    List<Habitacion> findByTipoIdAndActivaIsTrue(String tipoId);
    List<Habitacion> findByActivaIsTrue();
    boolean existsByNumero(Integer numero);
    @Query("""
        SELECT h
        FROM Habitacion h
        WHERE h.tipo.id = :tipoId
          AND h.activa = TRUE
          AND NOT EXISTS (
              SELECT 1
              FROM Estancia e
              WHERE e.habitacionAsignada.id = h.id
                AND e.reserva.estado = 'CONFIRMADA'
                AND (e.salida <= :entrada AND e.salida >= :entrada)
          )
        ORDER BY h.numero
    """)
    List<Habitacion> findHabitacionesDisponibles(
            @Param("tipoId") String tipoId,
            @Param("entrada") LocalDate entrada,
            @Param("salida") LocalDate salida
    );
}