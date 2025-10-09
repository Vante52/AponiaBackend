package com.aponia.aponia_hotel.controller.usuarios.dto;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioCreateRequest {

    private String id;
    private String email;
    private String password;
    private Usuario.UserRole rol;
    private ClientePerfil clientePerfil;
    private EmpleadoPerfil empleadoPerfil;
}