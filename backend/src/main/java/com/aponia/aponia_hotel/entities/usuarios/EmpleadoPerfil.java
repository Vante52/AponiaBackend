package com.aponia.aponia_hotel.entities.usuarios;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados_perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoPerfil {

    @Id
    @Column(name = "usuario_id", length = 36)
    private String usuarioId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "telefono", length = 25)
    private String telefono;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "salario", precision = 12, scale = 2)
    private BigDecimal salario;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDate fechaContratacion = LocalDate.now();
}