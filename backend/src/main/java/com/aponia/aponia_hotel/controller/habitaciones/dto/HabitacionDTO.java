package com.aponia.aponia_hotel.controller.habitaciones.dto;

import lombok.Data;

@Data
public class HabitacionDTO {
    private String id;
    private Integer numero;
    private Boolean activa;
    private String tipoId;
    private String tipoNombre;
}
