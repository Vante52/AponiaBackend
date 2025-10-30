package com.aponia.aponia_hotel.entities.reservas;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.pagos.Pago;
import com.aponia.aponia_hotel.entities.pagos.ResumenPago;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "codigo", nullable = false, unique = true, length = 32)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({ "passwordHash", "empleadoPerfil", "clientePerfil", "hibernateLazyInitializer", "handler" })
    private Usuario cliente;

    // @CreationTimestamp
    // @Column(name = "fecha_creacion", nullable = false, updatable = false)
    // private LocalDateTime fechaCreacion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "estado", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "reserva", "hibernateLazyInitializer", "handler" })
    private List<Estancia> estancias;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReservaServicio> reservasServicios;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Pago> pagos;

    @OneToOne(mappedBy = "reserva", fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private ResumenPago resumenPago;

    public enum EstadoReserva {
        CONFIRMADA,
        CANCELADA,
        COMPLETADA
    }
}