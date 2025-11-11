package com.aponia.aponia_hotel.controller.habitaciones.dto;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface HabitacionMapper {
    HabitacionMapper INSTANCE = Mappers.getMapper(HabitacionMapper.class);

    @Mapping(target = "tipoId", expression = "java(habitacion.getTipo() != null ? habitacion.getTipo().getId() : null)")
    @Mapping(target = "tipoNombre", expression = "java(habitacion.getTipo() != null ? habitacion.getTipo().getNombre() : null)")
    HabitacionDTO convert(Habitacion habitacion);
}
