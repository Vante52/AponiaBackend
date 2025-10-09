package com.aponia.aponia_hotel.entities.servicios;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "lugar", nullable = false, length = 120)
    private String lugar;

    @Column(name = "precio_por_persona", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPorPersona;

    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @Column(name = "capacidad_maxima")
    private Integer capacidadMaxima;

    // Evitar ciclos JSON (recomendación del video)
    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ServicioDisponibilidad> disponibilidades;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Imagen> imagenes;

    @OneToMany(mappedBy = "servicio", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ReservaServicio> reservasServicios;
}