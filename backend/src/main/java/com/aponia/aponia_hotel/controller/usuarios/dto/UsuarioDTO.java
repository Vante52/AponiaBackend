package com.aponia.aponia_hotel.controller.usuarios.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UsuarioDTO {
    private String id;
    private String email;
    private String rol;
    private String nombreCompleto;
    private String telefono;
    private String cargo;
    private BigDecimal salario;
    private LocalDate fechaContratacion;
    private LocalDateTime fechaRegistro;
}
