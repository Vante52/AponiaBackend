package com.aponia.aponia_hotel.service.reservas;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.EstanciaRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.pagos.ResumenPagoRepository;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository repository;
    private final EstanciaRepository estanciaRepository;
    private final HabitacionTipoRepository habitacionTipoRepository;
    private final ResumenPagoRepository resumenPagoRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaServiceImpl(
            ReservaRepository repository,
            EstanciaRepository estanciaRepository,
            HabitacionTipoRepository habitacionTipoRepository,
            ResumenPagoRepository resumenPagoRepository,
            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.estanciaRepository = estanciaRepository;
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
                Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.CONFIRMADA));
    }

    private ResumenPago prepararResumenPago(Reserva reserva) {
    // 1️⃣ Buscar si ya existe un resumen por el ID de la reserva
    Optional<ResumenPago> existente = resumenPagoRepository.findByReservaId(reserva.getId());

    ResumenPago resumen = existente.orElseGet(ResumenPago::new);
    resumen.setReserva(reserva);

    // 2️⃣ Calcular totales
    BigDecimal totalHabitaciones = calcularTotalHabitaciones(reserva);
    BigDecimal totalServicios = calcularTotalServicios(reserva);
    BigDecimal totalReserva = totalHabitaciones.add(totalServicios);

    resumen.setTotalHabitaciones(totalHabitaciones);
    resumen.setTotalServicios(totalServicios);
    resumen.setTotalReserva(totalReserva);

    if (resumen.getTotalPagado() == null) {
        resumen.setTotalPagado(BigDecimal.ZERO);
    }

    resumen.setSaldoPendiente(totalReserva.subtract(resumen.getTotalPagado()));

    return resumen;
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

    reserva.setEstado(EstadoReserva.PENDIENTE);
    reserva.setResumenPago(null);

    // 1️⃣ Guarda la reserva sin resumen (persistencia segura)
    Reserva guardada = repository.save(reserva);

    // 2️⃣ Crea el resumen asociado
    ResumenPago resumen = prepararResumenPago(guardada);
    resumenPagoRepository.save(resumen); // ⚡ GUARDAR AQUÍ directamente

    // 3️⃣ Asocia el resumen en memoria, pero no vuelvas a guardar la reserva
    guardada.setResumenPago(resumen);

    // 4️⃣ Devuelve la reserva ya completa
    return guardada;
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
                .orElseThrow(
                        () -> new IllegalArgumentException("No se encontró la reserva con ID: " + reserva.getId()));

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
    public boolean verificarDisponibilidad(String tipoHabitacionId, LocalDate entrada, LocalDate salida,
            int numeroHuespedes) {
        Optional<HabitacionTipo> tipo = habitacionTipoRepository.findById(tipoHabitacionId);
        if (tipo.isEmpty() || !tipo.get().getActiva() || tipo.get().getAforoMaximo() < numeroHuespedes) {
            return false;
        }

        long habitacionesOcupadas = estanciaRepository.contarHabitacionesOcupadas(
                tipoHabitacionId, entrada, salida);

        long totalHabitaciones = tipo.get().getHabitaciones().stream()
                .filter(h -> h.getActiva())
                .count();

        return habitacionesOcupadas < totalHabitaciones;
    }

    @Override
    public Reserva crearReservaCliente(String clienteId, String tipoHabitacionId, LocalDate entrada, LocalDate salida,
            Integer numeroHuespedes, String notas) {
        Objects.requireNonNull(clienteId, "El cliente es requerido");
        Objects.requireNonNull(tipoHabitacionId, "El tipo de habitación es requerido");
        Objects.requireNonNull(entrada, "La fecha de entrada es requerida");
        Objects.requireNonNull(salida, "La fecha de salida es requerida");
        Objects.requireNonNull(numeroHuespedes, "El número de huéspedes es requerido");

        if (!salida.isAfter(entrada)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la de entrada");
        }
        if (numeroHuespedes <= 0) {
            throw new IllegalArgumentException("El número de huéspedes debe ser positivo");
        }

        String clienteIdValido = validarId(clienteId, "del cliente");
        Usuario cliente = usuarioRepository.findById(clienteIdValido)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el cliente indicado"));

        String tipoHabitacionIdValido = validarId(tipoHabitacionId, "del tipo de habitación");
        HabitacionTipo tipoHabitacion = habitacionTipoRepository.findById(tipoHabitacionIdValido)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el tipo de habitación solicitado"));

        if (!Boolean.TRUE.equals(tipoHabitacion.getActiva())) {
            throw new IllegalStateException("El tipo de habitación no está disponible actualmente");
        }
        if (tipoHabitacion.getAforoMaximo() < numeroHuespedes) {
            throw new IllegalArgumentException("El número de huéspedes excede la capacidad del tipo de habitación");
        }

        if (!verificarDisponibilidad(tipoHabitacionId, entrada, salida, numeroHuespedes)) {
            throw new IllegalStateException("No hay disponibilidad para las fechas seleccionadas");
        }

        long noches = ChronoUnit.DAYS.between(entrada, salida);
        if (noches <= 0) {
            throw new IllegalArgumentException("La estancia debe incluir al menos una noche");
        }

        Reserva reserva = new Reserva();
        reserva.setId(UUID.randomUUID().toString());
        reserva.setCodigo(generarCodigoReserva());
        reserva.setCliente(cliente);
        reserva.setNotas(notas);

        Estancia estancia = new Estancia();
        estancia.setId(UUID.randomUUID().toString());
        estancia.setCheckIn(Boolean.FALSE);
        estancia.setCheckOut(Boolean.FALSE);
        estancia.setEntrada(entrada);
        estancia.setSalida(salida);
        estancia.setNumeroHuespedes(numeroHuespedes);
        estancia.setReserva(reserva);
        estancia.setTipoHabitacion(tipoHabitacion);
        estancia.setPrecioPorNoche(tipoHabitacion.getPrecioPorNoche());
        BigDecimal totalEstadia = tipoHabitacion.getPrecioPorNoche()
                .multiply(BigDecimal.valueOf(noches));
        estancia.setTotalEstadia(totalEstadia);

        reserva.setEstancias(List.of(estancia));

        Reserva nuevaReserva = crear(reserva);

        nuevaReserva.getEstancias().forEach(e -> {
            e.getEntrada();
            if (e.getTipoHabitacion() != null) {
                e.getTipoHabitacion().getNombre();
            }
        });
        if (nuevaReserva.getResumenPago() != null) {
            nuevaReserva.getResumenPago().getTotalReserva();
        }

        return nuevaReserva;
    }

    @Override
    public double calcularTotalReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        ResumenPago resumen = reserva.getResumenPago();
        return resumen.getTotalReserva().doubleValue();
    }

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

    // 🔹 Nueva versión flexible (reemplaza normalizarUuid)
    private String validarId(String valor, String descripcionCampo) {
        String limpio = valor == null ? null : valor.trim();
        if (limpio == null || limpio.isEmpty()) {
            throw new IllegalArgumentException("El identificador " + descripcionCampo + " es requerido");
        }
        return limpio;
    }
}
