package com.aponia.aponia_hotel.service.resources;

import com.aponia.aponia_hotel.entities.resources.Imagen;
import java.util.List;
import java.util.Optional;

public interface ImagenService {
    /**
     * Lista todas las imágenes
     */
    List<Imagen> listar();

    /**
     * Lista las imágenes asociadas a un servicio
     */
    List<Imagen> listarPorServicio(String servicioId);

    /**
     * Lista las imágenes asociadas a un tipo de habitación
     */
    List<Imagen> listarPorTipoHabitacion(String tipoHabitacionId);

    /**
     * Crea una nueva imagen
     */
    Imagen crear(Imagen imagen);

    /**
     * Obtiene una imagen por su ID
     */
    Optional<Imagen> obtener(String id);

    /**
     * Actualiza una imagen existente
     */
    Imagen actualizar(Imagen imagen);

    /**
     * Elimina una imagen por su ID
     */
    void eliminar(String id);

    // Métodos deprecados o no utilizados
    // @deprecated Usar listarPorServicio() en su lugar
    // List<Imagen> findByServicioId(String servicioId);

    // @deprecated Usar listarPorTipoHabitacion() en su lugar
    // List<Imagen> findByTipoHabitacionId(String tipoHabitacionId);
}