package com.aponia.aponia_hotel.entities.pagos;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumen_pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResumenPago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", unique = true, nullable = false)
    @JsonIgnore
    private Reserva reserva;

    @Column(name = "total_habitaciones", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalHabitaciones = BigDecimal.ZERO;

    @Column(name = "total_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalServicios = BigDecimal.ZERO;

    @Column(name = "total_reserva", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalReserva = BigDecimal.ZERO;

    @Column(name = "total_pagado", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPagado = BigDecimal.ZERO;

    @Column(name = "saldo_pendiente", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    @UpdateTimestamp
    @Column(name = "ultima_actualizacion", nullable = false)
    private LocalDateTime ultimaActualizacion;

    @PrePersist
    @PreUpdate
    public void validate() {
        if (totalHabitaciones == null || totalHabitaciones.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total de habitaciones debe ser no negativo");
        }
        if (totalServicios == null || totalServicios.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total de servicios debe ser no negativo");
        }
        if (totalReserva == null || totalReserva.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total de la reserva debe ser no negativo");
        }
        if (totalPagado == null || totalPagado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("El total pagado debe ser no negativo");
        }
        if (reserva == null) {
            throw new IllegalStateException("La reserva es requerida");
        }

        // Calcular totales
        totalReserva = totalHabitaciones.add(totalServicios);
        saldoPendiente = totalReserva.subtract(totalPagado);
    }
}
