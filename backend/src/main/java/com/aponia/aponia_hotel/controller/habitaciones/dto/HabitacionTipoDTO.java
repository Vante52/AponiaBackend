package com.aponia.aponia_hotel.controller.habitaciones.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HabitacionTipoDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private Integer aforoMaximo;
    private BigDecimal precioPorNoche;
    private Boolean activa;
    private List<String> imagenes;
}
