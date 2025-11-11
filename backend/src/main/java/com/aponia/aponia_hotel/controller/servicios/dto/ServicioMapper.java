package com.aponia.aponia_hotel.controller.servicios.dto;

import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ServicioMapper {
    ServicioMapper INSTANCE = Mappers.getMapper(ServicioMapper.class);

    @Mapping(target = "imagenesUrls", expression = "java(mapImagenes(servicio))")
    ServicioDTO convert(Servicio servicio);

    List<ServicioDTO> convert(List<Servicio> servicios);

    default List<String> mapImagenes(Servicio servicio) {
        if (servicio.getImagenes() == null) {
            return Collections.emptyList();
        }
        return servicio.getImagenes().stream()
                .map(Imagen::getUrl)
                .collect(Collectors.toList());
    }
}
