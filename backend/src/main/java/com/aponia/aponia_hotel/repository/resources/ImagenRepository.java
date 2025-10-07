package com.aponia.aponia_hotel.repository.resources;

import com.aponia.aponia_hotel.entities.resources.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, String> {
    List<Imagen> findByServicioId(String servicioId);
    List<Imagen> findByTipoHabitacionId(String tipoHabitacionId);
}