package com.aponia.aponia_hotel.entities.pagos;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoPagoConverter implements AttributeConverter<Pago.TipoPago, String> {

    @Override
    public String convertToDatabaseColumn(Pago.TipoPago tipo) {
        return tipo == null ? null : tipo.name().toLowerCase(); // guarda como 'anticipo'
    }

    @Override
    public Pago.TipoPago convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return Pago.TipoPago.valueOf(dbData.trim().toUpperCase()); // lee como TipoPago.ANTICIPO
    }
}
