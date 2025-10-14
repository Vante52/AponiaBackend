package com.aponia.aponia_hotel.entities.resources;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Imagen {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    @JsonBackReference("imagen-servicio")
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_habitacion_id")
    @JsonBackReference("imagen-tipo-habitacion")
    private HabitacionTipo tipoHabitacion;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    // ✅ Generar automáticamente el ID si no viene
    @PrePersist
    public void ensureId() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        validate();
    }

    @PreUpdate
    public void validate() {
        if ((servicio != null && tipoHabitacion != null) ||
            (servicio == null && tipoHabitacion == null)) {
            throw new IllegalStateException("Exactly one of servicio or tipoHabitacion must be set");
        }
    }
}
