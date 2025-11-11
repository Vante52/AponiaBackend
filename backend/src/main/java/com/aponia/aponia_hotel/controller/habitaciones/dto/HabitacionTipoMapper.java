package com.aponia.aponia_hotel.controller.habitaciones.dto;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface HabitacionTipoMapper {
    HabitacionTipoMapper INSTANCE = Mappers.getMapper(HabitacionTipoMapper.class);

    @Mapping(target = "imagenes", expression = "java(mapImagenes(tipo))")
    HabitacionTipoDTO convert(HabitacionTipo tipo);

    List<HabitacionTipoDTO> convert(List<HabitacionTipo> tipos);

    default List<String> mapImagenes(HabitacionTipo tipo) {
        if (tipo.getImagenes() == null) {
            return Collections.emptyList();
        }
        return tipo.getImagenes().stream()
                .map(Imagen::getUrl)
                .collect(Collectors.toList());
    }
}