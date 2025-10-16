package com.aponia.aponia_hotel.entities.resources;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Entidad que representa los recursos gráficos asociados a servicios o tipos de habitación.
 * Cada registro debe vincularse exclusivamente con una de las dos relaciones para evitar
 * ambigüedades al momento de mostrar la imagen en el frontend.
 */
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

    /**
     * Genera automáticamente el identificador cuando aún no se ha asignado desde la capa superior
     * y realiza una validación estructural básica antes de insertar el registro.
     */
    @PrePersist
    public void ensureId() {
        if (id == null || id.isBlank()) {
            id = java.util.UUID.randomUUID().toString();
        }
        validate();
    }

    /**
     * Comprueba que la entidad esté asociada exactamente a un servicio o a un tipo de habitación.
     * De este modo se mantiene la coherencia con la lógica de negocio documentada en los controladores.
     */
    @PreUpdate
    public void validate() {
        if ((servicio != null && tipoHabitacion != null) ||
            (servicio == null && tipoHabitacion == null)) {
            throw new IllegalStateException("Exactly one of servicio or tipoHabitacion must be set");
        }
    }
}
