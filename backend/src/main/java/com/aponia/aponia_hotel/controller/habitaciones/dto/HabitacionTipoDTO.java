package com.aponia.aponia_hotel.controller.habitaciones.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class HabitacionTipoDTO {
    private String id;
    private String nombre;
    private String descripcion;
    private Integer aforoMaximo;
    private BigDecimal precioPorNoche;
    private Boolean activa;
    private List<String> imagenes;
}
