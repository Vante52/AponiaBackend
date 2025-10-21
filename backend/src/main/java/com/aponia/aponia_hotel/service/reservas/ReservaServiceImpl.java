package com.aponia.aponia_hotel.service.reservas;

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

    /*  private ResumenPago prepararResumenPago(Reserva reserva) {
    // 1️⃣ Buscar si ya existe un resumen por el ID de la reserva
    Optional<ResumenPago> existente = resumenPagoRepository.findByReservaId(reserva.getId());

    ResumenPago resumen = existente.orElseGet(ResumenPago::new);
    resumen.setReservaId(reserva.getId());
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
}*/


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
    
        // 1️⃣ Guardar reserva primero (sin resumen)
        Reserva guardada = repository.save(reserva);
        // ✅ 2️⃣ Crear resumen INDEPENDIENTE
        ResumenPago resumen = new ResumenPago();
        resumen.setReservaId(guardada.getId());

         // Calcular totales
        BigDecimal totalHabitaciones = calcularTotalHabitaciones(guardada);
        BigDecimal totalServicios = calcularTotalServicios(guardada);
        BigDecimal totalReserva = totalHabitaciones.add(totalServicios);
        
        resumen.setTotalHabitaciones(totalHabitaciones);
        resumen.setTotalServicios(totalServicios);
        resumen.setTotalReserva(totalReserva);
        resumen.setTotalPagado(BigDecimal.ZERO);
        resumen.setSaldoPendiente(totalReserva);

        // ✅ 3️⃣ Guardar resumen
        resumenPagoRepository.save(resumen);

        // ✅ NO asociar en memoria, solo retornar la reserva
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
        if (reserva.isPresent() && reserva.get().getEstado() == EstadoReserva.COMPLETADA) {
            throw new IllegalStateException("No se pueden eliminar reservas completadas");
        }
        repository.deleteById(id);
    }

    /*@Override
    public void confirmarReserva(String id) {
        Reserva reserva = obtenerYValidar(id);
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
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
    }*/
    
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
        
        if (tipo.isEmpty() || !tipo.get().getActiva() || tipo.get().getAforoMaximo() < numeroHuespedes) {
            return false;
        }

        // Usar la query mejorada que verifica solapamientos
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

    // Validaciones básicas
    Objects.requireNonNull(clienteId, "El cliente es requerido");
    Objects.requireNonNull(tipoHabitacionId, "El tipo de habitación es requerido");
    Objects.requireNonNull(entrada, "La fecha de entrada es requerida");
    Objects.requireNonNull(salida, "La fecha de salida es requerida");
    Objects.requireNonNull(numeroHuespedes, "El número de huéspedes es requerido");

    if (entrada.isBefore(LocalDate.now())) {
        throw new IllegalArgumentException("La fecha de entrada no puede ser en el pasado");
    }

    if (!salida.isAfter(entrada)) {
        throw new IllegalArgumentException("La fecha de salida debe ser posterior a la entrada");
    }

    if (numeroHuespedes <= 0) {
        throw new IllegalArgumentException("El número de huéspedes debe ser positivo");
    }

    // Validar cliente
    String clienteIdValido = validarId(clienteId, "del cliente");
    Usuario cliente = usuarioRepository.findById(clienteIdValido)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

    // Validar tipo de habitación
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

    // BUSCAR HABITACIÓN DISPONIBLE
    List<Habitacion> disponibles = habitacionRepository.findHabitacionesDisponibles(
            tipoHabitacionIdValido, entrada, salida
    );

    if (disponibles.isEmpty()) {
        throw new IllegalStateException(
            String.format("No hay habitaciones disponibles del tipo '%s' para las fechas seleccionadas", 
                tipoHabitacion.getNombre())
        );
    }

    // Asignar primera habitación disponible
    Habitacion habitacionAsignada = disponibles.get(0);

    // Calcular total
    long noches = ChronoUnit.DAYS.between(entrada, salida);
    if (noches <= 0) {
        throw new IllegalArgumentException("La estancia debe incluir al menos una noche");
    }
    BigDecimal totalEstadia = tipoHabitacion.getPrecioPorNoche()
            .multiply(BigDecimal.valueOf(noches));

    // Crear RESERVA en estado CONFIRMADA
    Reserva reserva = new Reserva();
    reserva.setId(UUID.randomUUID().toString());
    reserva.setCodigo(generarCodigoReserva());
    reserva.setCliente(cliente);
    reserva.setEstado(EstadoReserva.CONFIRMADA); 
    reserva.setNotas(notas);

    // Crear ESTANCIA
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
    // Guardar usando el método crear() existente
    Reserva reservaGuardada = repository.save(reserva);

    // Crear resumen de pagos
    ResumenPago resumen = new ResumenPago();
    resumen.setReservaId(reservaGuardada.getId());  // ✅ Solo ID
    resumen.setTotalHabitaciones(totalEstadia);
    resumen.setTotalServicios(BigDecimal.ZERO);
    resumen.setTotalReserva(totalEstadia);
    resumen.setTotalPagado(BigDecimal.ZERO);
    resumen.setSaldoPendiente(totalEstadia);


    resumenPagoRepository.save(resumen);

    return reservaGuardada;
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
