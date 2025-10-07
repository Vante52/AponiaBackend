package com.aponia.aponia_hotel.service.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;

public interface ReservaService {
    List<Reserva> listar();
    Reserva crear(Reserva reserva);
    Optional<Reserva> obtener(String id);
    Reserva actualizar(Reserva reserva);
    void eliminar(String id);
    Optional<Reserva> findByCodigo(String codigo);
    void confirmarReserva(String id);
    void cancelarReserva(String id);
    public List<Reserva> listarPorCliente(String clienteId);
    public List<Reserva> listarPorEstado(EstadoReserva estado);
    public List<Reserva> listarReservasActivas(String clienteId);
    public void completarReserva(String id);
    public boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate entrada, LocalDate salida, int numeroHuespedes);
    public double calcularTotalReserva(String id);
}