package com.aponia.aponia_hotel.service.reservas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.pagos.ResumenPagoRepository;
import com.aponia.aponia_hotel.repository.reservas.EstanciaRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repository;
    private final EstanciaRepository estanciaRepository;
    private final HabitacionRepository habitacionRepository;
    private final HabitacionTipoRepository habitacionTipoRepository;
    private final ResumenPagoRepository resumenPagoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaServiceImpl(
            ReservaRepository repository,
            EstanciaRepository estanciaRepository,
            HabitacionRepository habitacionRepository,
            HabitacionTipoRepository habitacionTipoRepository,
            ResumenPagoRepository resumenPagoRepository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.estanciaRepository = estanciaRepository;
        this.habitacionRepository = habitacionRepository;
        this.habitacionTipoRepository = habitacionTipoRepository;
        this.resumenPagoRepository = resumenPagoRepository;
        this.usuarioRepository = usuarioRepository;
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
                Arrays.asList(EstadoReserva.CONFIRMADA));
    }

    @Override
    public Reserva crear(Reserva reserva) {
        validarReserva(reserva);

        if (reserva.getId() == null || reserva.getId().isBlank()) {
            reserva.setId(UUID.randomUUID().toString());
        }

        if (repository.existsByCodigo(reserva.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una reserva con ese código");
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);

        if (reserva.getFechaCreacion() == null) {
            reserva.setFechaCreacion(LocalDateTime.now());
        }

        return repository.save(reserva);
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

        Reserva reservaExistente = repository.findById(reserva.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró la reserva con ID: " + reserva.getId()));

        if (!reservaExistente.getCodigo().equals(reserva.getCodigo())
                && repository.existsByCodigo(reserva.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una reserva con ese código");
        }

        return repository.save(reserva);
    }

    @Override
    public void eliminar(String id) {
        Optional<Reserva> reserva = repository.findById(id);

        if (reserva.isPresent() && reserva.get().getEstado() == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException("No se pueden eliminar reservas completadas");
        }

        repository.deleteById(id);
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
    @Transactional(readOnly = true)
    public boolean verificarDisponibilidad(
            String tipoHabitacionId,
            LocalDate entrada,
            LocalDate salida,
            int numeroHuespedes) {

        Optional<HabitacionTipo> tipo = habitacionTipoRepository.findById(tipoHabitacionId);

        if (tipo.isEmpty() || !tipo.get().getActiva()
                || tipo.get().getAforoMaximo() < numeroHuespedes) {
            return false;
        }

        List<Habitacion> disponibles = habitacionRepository.findHabitacionesDisponibles(
                tipoHabitacionId, entrada, salida
        );

        return !disponibles.isEmpty();
    }

    @Override
    public Reserva crearReservaCliente(
            String clienteId,
            String tipoHabitacionId,
            LocalDate entrada,
            LocalDate salida,
            Integer numeroHuespedes,
            String notas) {

        // Validar parámetros requeridos
        Objects.requireNonNull(clienteId, "El cliente es requerido");
        Objects.requireNonNull(tipoHabitacionId, "El tipo de habitación es requerido");
        Objects.requireNonNull(entrada, "La fecha de entrada es requerida");
        Objects.requireNonNull(salida, "La fecha de salida es requerida");
        Objects.requireNonNull(numeroHuespedes, "El número de huéspedes es requerido");

        // Validar fechas
        if (entrada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser en el pasado");
        }

        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la entrada");
        }

        if (numeroHuespedes <= 0) {
            throw new IllegalArgumentException("El número de huéspedes debe ser positivo");
        }

        // Validar y obtener el cliente
        String clienteIdValido = validarId(clienteId, "del cliente");
        Usuario cliente = usuarioRepository.findById(clienteIdValido)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Validar y obtener el tipo de habitación
        String tipoHabitacionIdValido = validarId(tipoHabitacionId, "del tipo de habitación");
        HabitacionTipo tipoHabitacion = habitacionTipoRepository.findById(tipoHabitacionIdValido)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de habitación no encontrado"));

        if (!Boolean.TRUE.equals(tipoHabitacion.getActiva())) {
            throw new IllegalStateException("El tipo de habitación no está disponible");
        }

        if (tipoHabitacion.getAforoMaximo() < numeroHuespedes) {
            throw new IllegalArgumentException(
                    String.format("El número de huéspedes (%d) excede la capacidad máxima (%d)",
                            numeroHuespedes, tipoHabitacion.getAforoMaximo())
            );
        }

        // Buscar habitación disponible
        List<Habitacion> disponibles = habitacionRepository.findHabitacionesDisponibles(
                tipoHabitacionIdValido, entrada, salida
        );

        if (disponibles.isEmpty()) {
            throw new IllegalStateException(
                    String.format("No hay habitaciones disponibles del tipo '%s' para las fechas seleccionadas",
                            tipoHabitacion.getNombre())
            );
        }

        Habitacion habitacionAsignada = disponibles.get(0);

        // Calcular el total de la estadía
        long noches = ChronoUnit.DAYS.between(entrada, salida);
        if (noches <= 0) {
            throw new IllegalArgumentException("La estancia debe incluir al menos una noche");
        }

        BigDecimal totalEstadia = tipoHabitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));

        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID().toString());
        reserva.setCodigo(generarCodigoReserva());
        reserva.setCliente(cliente);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setNotas(notas);

        reserva.setFechaCreacion(LocalDateTime.now());

        // Crear la estancia asociada
        Estancia estancia = new Estancia();
        estancia.setId(UUID.randomUUID().toString());
        estancia.setReserva(reserva);
        estancia.setTipoHabitacion(tipoHabitacion);
        estancia.setHabitacionAsignada(habitacionAsignada);
        estancia.setEntrada(entrada);
        estancia.setSalida(salida);
        estancia.setNumeroHuespedes(numeroHuespedes);
        estancia.setPrecioPorNoche(tipoHabitacion.getPrecioPorNoche());
        estancia.setTotalEstadia(totalEstadia);
        estancia.setCheckIn(Boolean.FALSE);
        estancia.setCheckOut(Boolean.FALSE);

        reserva.setEstancias(List.of(estancia));

        // Guardar la reserva y forzar el flush para completar la transacción
        Reserva reservaGuardada = repository.saveAndFlush(reserva);

        // Crear o actualizar el resumen de pagos
        crearResumenPagoSeguro(reservaGuardada.getId(), totalEstadia);

        return reservaGuardada;
    }

    /**
     * Crea o actualiza el resumen de pagos de forma segura, manejando posibles
     * conflictos de clave duplicada que puedan ocurrir por triggers de base de
     * datos.
     */
    private void crearResumenPagoSeguro(String reservaId, BigDecimal totalEstadia) {
        try {
            Optional<ResumenPago> existente = resumenPagoRepository.findByReservaId(reservaId);

            ResumenPago resumen;
            if (existente.isPresent()) {
                // Actualizar el resumen existente
                resumen = existente.get();
            } else {
                // Crear un nuevo resumen
                resumen = new ResumenPago();
                resumen.setReservaId(reservaId);
            }

            resumen.setTotalHabitaciones(totalEstadia);
            resumen.setTotalServicios(BigDecimal.ZERO);
            resumen.setTotalReserva(totalEstadia);
            resumen.setTotalPagado(BigDecimal.ZERO);
            resumen.setSaldoPendiente(totalEstadia);

            resumenPagoRepository.saveAndFlush(resumen);

        } catch (DataIntegrityViolationException e) {
            // Manejar violación de clave duplicada intentando actualizar el registro existente
            try {
                Optional<ResumenPago> resumen = resumenPagoRepository.findByReservaId(reservaId);
                if (resumen.isPresent()) {
                    ResumenPago r = resumen.get();
                    r.setTotalHabitaciones(totalEstadia);
                    r.setTotalServicios(BigDecimal.ZERO);
                    resumenPagoRepository.saveAndFlush(r);
                }
            } catch (Exception e2) {
                // Si falla la actualización, no propagamos el error ya que la reserva fue creada exitosamente
            }
        } catch (Exception e) {
            // Cualquier otro error no debe afectar la creación de la reserva
        }
    }

    @Override
    public double calcularTotalReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        ResumenPago resumen = reserva.getResumenPago();

        // Si no existe resumen, calcularlo desde las estancias
        if (resumen == null) {
            return calcularTotalHabitaciones(reserva).doubleValue();
        }

        return resumen.getTotalReserva().doubleValue();
    }

    // Métodos auxiliares privados
    private BigDecimal calcularTotalHabitaciones(Reserva reserva) {
        if (reserva.getEstancias() == null) {
            return BigDecimal.ZERO;
        }

        return reserva.getEstancias().stream()
                .map(Estancia::getTotalEstadia)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularTotalServicios(Reserva reserva) {
        if (reserva.getReservasServicios() == null) {
            return BigDecimal.ZERO;
        }

        return reserva.getReservasServicios().stream()
                .map(ReservaServicio::getTotalServicio)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generarCodigoReserva() {
        String codigo;
        do {
            codigo = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (repository.existsByCodigo(codigo));
        return codigo;
    }

    private Reserva obtenerYValidar(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró la reserva con ID: " + id));
    }

    private void validarReserva(Reserva reserva) {
        if (reserva.getCliente() == null) {
            throw new IllegalArgumentException("La reserva debe tener un cliente asignado");
        }

        if (reserva.getCodigo() == null || reserva.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("La reserva debe tener un código");
        }
    }

    private String validarId(String valor, String descripcionCampo) {
        String limpio = valor == null ? null : valor.trim();

        if (limpio == null || limpio.isEmpty()) {
            throw new IllegalArgumentException(
                    "El identificador " + descripcionCampo + " es requerido");
        }

        return limpio;
    }

    @Override
    @Transactional
    public Reserva actualizarReservaCliente(
            String reservaId,
            String tipoHabitacionId,
            LocalDate entrada,
            LocalDate salida,
            int numeroHuespedes,
            String notas) {

        // Obtener reserva existente
        Reserva reserva = repository.findById(reservaId)
                .orElseThrow(() -> new IllegalStateException("Reserva no encontrada"));

        // Validar que esté confirmada
        if (reserva.getEstado() != Reserva.EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se pueden modificar reservas confirmadas");
        }

        // Verificar disponibilidad con las nuevas fechas
        if (!verificarDisponibilidad(tipoHabitacionId, entrada, salida, numeroHuespedes)) {
            throw new IllegalStateException("No hay disponibilidad para las fechas seleccionadas");
        }

        // Obtener tipo de habitación
        HabitacionTipo tipoHabitacion = habitacionTipoRepository.findById(tipoHabitacionId)
                .orElseThrow(() -> new IllegalStateException("Tipo de habitación no encontrado"));

        // Actualizar la estancia (asumiendo que hay una sola estancia por reserva)
        if (reserva.getEstancias() != null && !reserva.getEstancias().isEmpty()) {
            Estancia estancia = reserva.getEstancias().get(0);

            estancia.setEntrada(entrada);
            estancia.setSalida(salida);
            estancia.setNumeroHuespedes(numeroHuespedes);
            estancia.setTipoHabitacion(tipoHabitacion);
            estancia.setPrecioPorNoche(tipoHabitacion.getPrecioPorNoche());
            // Recalcular total
            long noches = ChronoUnit.DAYS.between(entrada, salida);
            BigDecimal total = tipoHabitacion.getPrecioPorNoche().multiply(BigDecimal.valueOf(noches));
            estancia.setTotalEstadia(total);
        }

        // Actualizar notas
        reserva.setNotas(notas);

        return repository.save(reserva);
    }
}
