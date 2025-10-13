package com.aponia.aponia_hotel.repository.usuarios;
import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoPerfilRepository extends JpaRepository<EmpleadoPerfil, String> {

    List<EmpleadoPerfil> findByCargo(String cargo);

}