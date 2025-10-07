package com.aponia.aponia_hotel.service.pagos;

import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.pagos.Pago;
import com.aponia.aponia_hotel.entities.pagos.Pago.EstadoPago;
import com.aponia.aponia_hotel.entities.pagos.Pago.TipoPago;

public interface PagoService {
    /**
     * Lista todos los pagos
     */
    List<Pago> listar();

    /**
     * Lista los pagos por reserva
     */
    List<Pago> listarPorReserva(String reservaId);

    /**
     * Lista los pagos por reserva y estado
     */
    List<Pago> listarPorReservaYEstado(String reservaId, EstadoPago estado);

    /**
     * Lista los pagos por tipo
     */
    List<Pago> listarPorTipo(TipoPago tipo);

    /**
     * Crea un nuevo pago
     */
    Pago crear(Pago pago);

    /**
     * Obtiene un pago por su ID
     */
    Optional<Pago> obtener(String id);

    /**
     * Actualiza un pago existente
     */
    Pago actualizar(Pago pago);

    /**
     * Elimina un pago por su ID
     */
    void eliminar(String id);

    /**
     * Completa un pago pendiente
     */
    void completarPago(String id);

    /**
     * Marca un pago como fallido
     */
    void marcarPagoFallido(String id);

    /**
     * Procesa un reembolso
     */
    void procesarReembolso(String id);

    /**
     * Calcula el total de pagos completados para una reserva
     */
    double calcularTotalPagosCompletados(String reservaId);
}