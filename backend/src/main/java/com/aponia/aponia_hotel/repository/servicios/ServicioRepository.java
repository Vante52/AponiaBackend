package com.aponia.aponia_hotel.repository.servicios;

import com.aponia.aponia_hotel.entities.servicios.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, String> {
    boolean existsByNombre(String nombre);
}