package com.aponia.aponia_hotel.entities.pagos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.aponia.aponia_hotel.entities.reservas.Reserva;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPago tipo;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @CreationTimestamp
    @Column(name = "fecha", nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(name = "concepto", length = 200)
    private String concepto;

    public enum TipoPago {
        ANTICIPO,
        PAGO_PARCIAL,
        PAGO_COMPLETO,
        REEMBOLSO
    }

    public enum EstadoPago {
        PENDIENTE,
        COMPLETADO,
        FALLIDO,
        REEMBOLSADO
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El monto debe ser positivo");
        }
        if (tipo == null) {
            throw new IllegalStateException("El tipo de pago es requerido");
        }
        if (reserva == null) {
            throw new IllegalStateException("La reserva es requerida");
        }
    }
}