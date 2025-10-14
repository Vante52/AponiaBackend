package com.aponia.aponia_hotel.entities.habitaciones;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "habitaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    @JsonBackReference("habitacion-tipo")
    private HabitacionTipo tipo;

    @Column(name = "numero", nullable = false, unique = true)
    private Integer numero;

    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (numero == null || numero <= 0) {
            throw new IllegalStateException("El número debe ser positivo");
        }
    }
}
