package com.aponia.aponia_hotel.entities.pagos;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPagoConverter implements AttributeConverter<Pago.EstadoPago, String> {

    @Override
    public String convertToDatabaseColumn(Pago.EstadoPago estado) {
        return estado == null ? null : estado.name().toLowerCase();
    }

    @Override
    public Pago.EstadoPago convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Pago.EstadoPago.valueOf(dbData.trim().toUpperCase());
    }
}
