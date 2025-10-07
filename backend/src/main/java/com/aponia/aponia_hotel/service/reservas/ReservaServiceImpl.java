package com.aponia.aponia_hotel.service.reservas;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.EstanciaRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.pagos.ResumenPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repository;
    private final EstanciaRepository estanciaRepository;
    private final HabitacionTipoRepository habitacionTipoRepository;
    private final ResumenPagoRepository resumenPagoRepository;

    public ReservaServiceImpl(
            ReservaRepository repository,
            EstanciaRepository estanciaRepository,
            HabitacionTipoRepository habitacionTipoRepository,
            ResumenPagoRepository resumenPagoRepository) {
        this.repository = repository;
        this.estanciaRepository = estanciaRepository;
        this.habitacionTipoRepository = habitacionTipoRepository;
        this.resumenPagoRepository = resumenPagoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarPorEstado(EstadoReserva estado) {
        return repository.findByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reserva> listarReservasActivas(String clienteId) {
        return repository.findByClienteIdAndEstadoIn(
            clienteId,
            Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA)
        );
    }

    @Override
    public Reserva crear(Reserva reserva) {
        validarReserva(reserva);
        if (repository.existsByCodigo(reserva.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una reserva con ese código");
        }

        reserva.setEstado(EstadoReserva.PENDIENTE);
        Reserva nuevaReserva = repository.save(reserva);

        // Inicializar el resumen de pagos
        ResumenPago resumen = new ResumenPago();
        resumen.setReserva(nuevaReserva);
        resumenPagoRepository.save(resumen);

        return nuevaReserva;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reserva> findByCodigo(String codigo) {
        return repository.findByCodigo(codigo);
    }

    @Override
    public Reserva actualizar(Reserva reserva) {
        validarReserva(reserva);
        Optional<Reserva> existente = repository.findById(reserva.getId());
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la reserva con ID: " + reserva.getId());
        }

        Reserva reservaExistente = existente.get();
        if (!reservaExistente.getCodigo().equals(reserva.getCodigo()) &&
            repository.existsByCodigo(reserva.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una reserva con ese código");
        }

        return repository.save(reserva);
    }

    @Override
    public void eliminar(String id) {
        Optional<Reserva> reserva = repository.findById(id);
        if (reserva.isPresent() && reserva.get().getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden eliminar reservas pendientes");
        }
        repository.deleteById(id);
    }

    @Override
    public void confirmarReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar reservas pendientes");
        }

        // Verificar disponibilidad antes de confirmar
        for (Estancia estancia : reserva.getEstancias()) {
            if (!verificarDisponibilidad(
                    estancia.getTipoHabitacion().getId(),
                    estancia.getEntrada(),
                    estancia.getSalida(),
                    estancia.getNumeroHuespedes())) {
                throw new IllegalStateException("No hay disponibilidad para alguna de las habitaciones solicitadas");
            }
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        repository.save(reserva);
    }

    @Override
    public void cancelarReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        if (reserva.getEstado() == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una reserva completada");
        }

        reserva.setEstado(EstadoReserva.CANCELADA);
        repository.save(reserva);
    }

    @Override
    public void completarReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden completar reservas confirmadas");
        }

        reserva.setEstado(EstadoReserva.COMPLETADA);
        repository.save(reserva);
    }

    @Override
    public boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate entrada, LocalDate salida, int numeroHuespedes) {
        // Verificar que el tipo de habitación existe y tiene capacidad suficiente
        Optional<HabitacionTipo> tipo = habitacionTipoRepository.findById(tipoHabitacionId);
        if (tipo.isEmpty() || !tipo.get().getActiva() || tipo.get().getAforoMaximo() < numeroHuespedes) {
            return false;
        }

        // Contar cuántas habitaciones de este tipo están ocupadas en las fechas solicitadas
        long habitacionesOcupadas = estanciaRepository.contarHabitacionesOcupadas(
            tipoHabitacionId, entrada, salida);

        // Contar el total de habitaciones de este tipo
        long totalHabitaciones = tipo.get().getHabitaciones().stream()
            .filter(h -> h.getActiva())
            .count();

        return habitacionesOcupadas < totalHabitaciones;
    }

    @Override
    public double calcularTotalReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        ResumenPago resumen = reserva.getResumenPago();
        return resumen.getTotalReserva().doubleValue();
    }

    private Reserva obtenerYValidar(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con ID: " + id));
    }

    private void validarReserva(Reserva reserva) {
        if (reserva.getCliente() == null) {
            throw new IllegalArgumentException("La reserva debe tener un cliente asignado");
        }
        if (reserva.getCodigo() == null || reserva.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe tener un código");
        }
    }
}