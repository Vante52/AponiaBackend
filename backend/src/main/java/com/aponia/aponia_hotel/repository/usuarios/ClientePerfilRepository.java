package com.aponia.aponia_hotel.repository.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientePerfilRepository extends JpaRepository<ClientePerfil, String> {
}