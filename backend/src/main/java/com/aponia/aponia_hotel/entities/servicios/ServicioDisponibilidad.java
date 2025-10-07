package com.aponia.aponia_hotel.entities.servicios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "servicio_disponibilidad",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"servicio_id", "fecha", "hora_inicio"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDisponibilidad {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    @JsonIgnore // cortamos la referencia inversa para evitar recursión
    private Servicio servicio;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "capacidad_disponible", nullable = false)
    private Integer capacidadDisponible;

    @PrePersist
    @PreUpdate
    public void validateTimes() {
        if (horaInicio != null && horaFin != null && !horaFin.isAfter(horaInicio)) {
            throw new IllegalStateException("hora_fin must be after hora_inicio");
        }
        if (capacidadDisponible < 0) {
            throw new IllegalStateException("capacidad_disponible must be non-negative");
        }
    }
}