package com.aponia.aponia_hotel.service.pagos;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.pagos.ResumenPago;

public interface ResumenPagoService {
    List<ResumenPago> listar();
    ResumenPago crear(ResumenPago resumenPago);
    Optional<ResumenPago> obtener(String reservaId);
    public Optional<ResumenPago> obtenerPorReserva(String reservaId) ;
    ResumenPago actualizar(ResumenPago resumenPago);
    void eliminar(String reservaId);
    void actualizarResumen(String reservaId, BigDecimal totalHabitaciones, BigDecimal totalServicios, BigDecimal totalPagado);
}