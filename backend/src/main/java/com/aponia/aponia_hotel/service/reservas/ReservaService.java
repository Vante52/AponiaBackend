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
    //void confirmarReserva(String id);
    void cancelarReserva(String id);
    List<Reserva> listarPorCliente(String clienteId);
    List<Reserva> listarPorEstado(EstadoReserva estado);
    List<Reserva> listarReservasActivas(String clienteId);
    void completarReserva(String id);
    boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate entrada, LocalDate salida, int numeroHuespedes);
    double calcularTotalReserva(String id);
    Reserva crearReservaCliente(String clienteId, String tipoHabitacionId, LocalDate entrada, LocalDate salida, Integer numeroHuespedes, String notas);
}