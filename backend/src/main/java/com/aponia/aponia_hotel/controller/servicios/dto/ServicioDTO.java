package com.aponia.aponia_hotel.controller.servicios.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServicioDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private String lugar;
    private BigDecimal precioPorPersona;
    private Integer duracionMinutos;
    private Integer capacidadMaxima;
    private List<String> imagenes;
}


