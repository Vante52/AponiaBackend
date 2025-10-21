package com.aponia.aponia_hotel.service.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.EstanciaRepository;

@Service
@Transactional
public class EstanciaServiceImpl implements EstanciaService {

    private final EstanciaRepository repository;
    private final HabitacionRepository habitacionRepository;
    private final HabitacionTipoRepository habitacionTipoRepository;

    public EstanciaServiceImpl(
            EstanciaRepository repository,
            HabitacionRepository habitacionRepository,
            HabitacionTipoRepository habitacionTipoRepository) {
        this.repository = repository;
        this.habitacionRepository = habitacionRepository;
        this.habitacionTipoRepository = habitacionTipoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estancia> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estancia> listarPorReserva(String reservaId) {
        return repository.findByReservaId(reservaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estancia> listarCheckinsDelDia(String tipoHabitacionId, LocalDate fecha) {
        return repository.findCheckinsByTipoHabitacionAndFecha(tipoHabitacionId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estancia> listarCheckoutsDelDia(String tipoHabitacionId, LocalDate fecha) {
        return repository.findCheckoutsByTipoHabitacionAndFecha(tipoHabitacionId, fecha);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate checkIn, LocalDate checkOut, int numeroHuespedes) {
        Optional<HabitacionTipo> tipo = habitacionTipoRepository.findById(tipoHabitacionId);
        if (tipo.isEmpty() || !tipo.get().getActiva() || tipo.get().getAforoMaximo() < numeroHuespedes) {
            return false;
        }

        long habitacionesOcupadas = contarHabitacionesOcupadas(tipoHabitacionId, checkIn, checkOut);
        long totalHabitaciones = tipo.get().getHabitaciones().stream()
                .filter(Habitacion::getActiva)
                .count();

        return habitacionesOcupadas < totalHabitaciones;
    }

    @Override
    public Estancia asignarHabitacion(String estanciaId) {
        Optional<Estancia> estanciaOpt = repository.findById(estanciaId);
        if (estanciaOpt.isEmpty()) {
            throw new IllegalArgumentException("Estancia no encontrada");
        }

        Estancia estancia = estanciaOpt.get();
        if (estancia.getHabitacionAsignada() != null) {
            throw new IllegalStateException("La estancia ya tiene una habitación asignada");
        }

        List<Habitacion> habitacionesDisponibles = habitacionRepository.findHabitacionesDisponibles(
                estancia.getTipoHabitacion().getId(),
                estancia.getEntrada(),
                estancia.getSalida()
        );

        if (habitacionesDisponibles.isEmpty()) {
            throw new IllegalStateException("No hay habitaciones disponibles para las fechas solicitadas");
        }

        estancia.setHabitacionAsignada(habitacionesDisponibles.get(0));
        return repository.save(estancia);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarHabitacionesOcupadas(String tipoHabitacionId, LocalDate checkIn, LocalDate checkOut) {
        return repository.contarHabitacionesOcupadas(tipoHabitacionId, checkIn, checkOut);
    }

    @Override
    public Estancia crear(Estancia estancia) {
        validarEstancia(estancia);
        return repository.save(estancia);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Estancia> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public Estancia actualizar(Estancia estancia) {
        validarEstancia(estancia);
        return repository.save(estancia);
    }

    @Override
    public void eliminar(String id) {
        Optional<Estancia> estancia = repository.findById(id);
        if (estancia.isEmpty()) {
            throw new IllegalArgumentException("Estancia no encontrada");
        }
        if (estancia.get().getReserva() != null
                && estancia.get().getReserva().getEstado() != Reserva.EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("No se pueden eliminar estancias de reservas confirmadas o completadas");
        }
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Estancia> buscarConflictosFechas(String habitacionId, LocalDate checkIn, LocalDate checkOut) {
        return repository.findOverlappingStays(habitacionId, checkIn, checkOut);
    }

    private void validarEstancia(Estancia estancia) {
        if (estancia.getEntrada() == null || estancia.getSalida() == null) {
            throw new IllegalArgumentException("Las fechas de entrada y salida son requeridas");
        }
        if (!estancia.getSalida().isAfter(estancia.getEntrada())) {
            throw new IllegalArgumentException("La fecha de check-out debe ser posterior a la de check-in");
        }
        if (estancia.getNumeroHuespedes() <= 0) {
            throw new IllegalArgumentException("El número de huéspedes debe ser positivo");
        }
        if (estancia.getTipoHabitacion() == null) {
            throw new IllegalArgumentException("El tipo de habitación es requerido");
        }
        if (estancia.getHabitacionAsignada() != null) {
            validarDisponibilidadHabitacion(estancia);
        }
    }

    private void validarDisponibilidadHabitacion(Estancia estancia) {
        List<Estancia> conflictos = buscarConflictosFechas(
                estancia.getHabitacionAsignada().getId(),
                estancia.getEntrada(),
                estancia.getSalida()
        );

        if (!conflictos.isEmpty()
                && (conflictos.size() > 1 || !conflictos.get(0).getId().equals(estancia.getId()))) {
            throw new IllegalStateException("La habitación no está disponible para las fechas seleccionadas");
        }

        if (!estancia.getHabitacionAsignada().getTipo().equals(estancia.getTipoHabitacion())) {
            throw new IllegalArgumentException("La habitación asignada no corresponde al tipo solicitado");
        }
    }

    @Override
    public Optional<Estancia> obtenerEstanciaActivaPorHabitacion(String habitacionId) {
        try {
            // ✅ CONVERTIR el número al formato correcto
            String habitacionIdFormateado = habitacionId;
            if (habitacionId.matches("\\d+")) { // Si es solo números
                habitacionIdFormateado = "hab_" + habitacionId;
            }

            System.out.println("🔍 Service: Buscando estancia activa para: " + habitacionIdFormateado);
            Optional<Estancia> resultado = repository.findByHabitacionIdAndReservaActiva(habitacionIdFormateado);
            System.out.println("✅ Service: Resultado - " + (resultado.isPresent() ? "ENCONTRADO" : "NO ENCONTRADO"));
            return resultado;
        } catch (Exception e) {
            System.err.println("❌ Service Error: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
