package com.aponia.aponia_hotel.repository.servicios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aponia.aponia_hotel.entities.servicios.Servicio;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, String> {
    boolean existsByNombre(String nombre);

    //Optional<ResumenPago> findByReservaId(String reservaId);
}