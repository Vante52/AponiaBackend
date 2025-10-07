package com.aponia.aponia_hotel.service.servicios;

import com.aponia.aponia_hotel.entities.servicios.Servicio;
import java.util.List;
import java.util.Optional;

public interface ServicioService {
    /**
     * Lista todos los servicios
     */
    List<Servicio> listar();

    /**
     * Crea un nuevo servicio
     */
    Servicio crear(Servicio servicio);

    /**
     * Obtiene un servicio por su ID
     */
    Optional<Servicio> obtener(String id);

    /**
     * Actualiza un servicio existente
     */
    Servicio actualizar(Servicio servicio);

    /**
     * Elimina un servicio por su ID
     */
    void eliminar(String id);

    // @deprecated No se utiliza actualmente en la implementación
    // List<Servicio> findByActivo(boolean activo);
}