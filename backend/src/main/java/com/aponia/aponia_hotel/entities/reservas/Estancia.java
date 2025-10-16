package com.aponia.aponia_hotel.entities.reservas;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "estancias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Estancia {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "check_in", nullable = false)
    private Boolean checkIn = Boolean.FALSE;

    @Column(name = "check_out", nullable = false)
    private Boolean checkOut = Boolean.FALSE;

    @Column(name = "entrada", nullable = false)
    private LocalDate entrada;

    @Column(name = "salida", nullable = false)
    private LocalDate salida;

    @Column(name = "numero_huespedes", nullable = false)
    private Integer numeroHuespedes;

    @Column(name = "precio_por_noche", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPorNoche;

    @Column(name = "total_estadia", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEstadia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    @JsonIgnore
    private Reserva reserva;

    // En Estancia.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "tipo_habitacion_id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
private HabitacionTipo tipoHabitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habitacion_asignada")
    @JsonIgnore
    private Habitacion habitacionAsignada;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (entrada == null) {
            throw new IllegalStateException("La fecha de check-in es requerida");
        }
        if (salida == null) {
            throw new IllegalStateException("La fecha de check-out es requerida");
        }
        if (!salida.isAfter(entrada)) {
            throw new IllegalStateException("La fecha de check-out debe ser posterior a la de check-in");
        }
        if (numeroHuespedes == null || numeroHuespedes <= 0) {
            throw new IllegalStateException("El número de huéspedes debe ser positivo");
        }
        if (precioPorNoche == null || precioPorNoche.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El precio por noche debe ser no negativo");
        }
        if (totalEstadia == null || totalEstadia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total de la estadía debe ser no negativo");
        }
        if (tipoHabitacion == null) {
            throw new IllegalStateException("El tipo de habitación es requerido");
        }
    }
}