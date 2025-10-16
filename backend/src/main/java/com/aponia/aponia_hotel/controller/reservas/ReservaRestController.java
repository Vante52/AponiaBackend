package com.aponia.aponia_hotel.controller.reservas;

import com.aponia.aponia_hotel.controller.reservas.dto.ReservaHabitacionRequest;
import com.aponia.aponia_hotel.controller.reservas.dto.ReservaHabitacionResponse;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.service.reservas.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ReservaRestController {

    private final ReservaService service;

    public ReservaRestController(ReservaService service) {
        this.service = service;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todas las reservas")
    public List<Reserva> findAll() { return service.listar(); }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Lista reservas por cliente")
    public List<Reserva> findByCliente(@PathVariable String clienteId) {
        return service.listarPorCliente(clienteId);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Lista reservas por estado")
    public List<Reserva> findByEstado(@PathVariable Reserva.EstadoReserva estado) {
        return service.listarPorEstado(estado);
    }

    @GetMapping("/activas/{clienteId}")
    @Operation(summary = "Lista reservas activas (pendientes/confirmadas) de un cliente")
    public List<Reserva> activas(@PathVariable String clienteId) {
        return service.listarReservasActivas(clienteId);
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Busca reserva por código")
    public Reserva findByCodigo(@PathVariable String codigo) {
        return service.findByCodigo(codigo).orElse(null);
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una reserva por ID")
    public Reserva findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    @GetMapping("/disponible")
    @Operation(summary = "Verifica disponibilidad (tipo, fechas, huéspedes)")
    public boolean verificarDisponibilidad(
            @RequestParam String tipoHabitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entrada,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam int numeroHuespedes) {
        return service.verificarDisponibilidad(tipoHabitacionId, entrada, salida, numeroHuespedes);
    }

    @GetMapping("/{id}/total")
    @Operation(summary = "Calcula el total de la reserva")
    public double total(@PathVariable String id) {
        return service.calcularTotalReserva(id);
    }

    @PostMapping("/cliente/{clienteId}/habitaciones")
    @Operation(summary = "Permite a un cliente crear una reserva para un tipo de habitación")
    public ResponseEntity<ReservaHabitacionResponse> reservarHabitacion(
            @PathVariable String clienteId,
            @RequestBody ReservaHabitacionRequest request) {
        Reserva reserva = service.crearReservaCliente(
                clienteId,
                request.tipoHabitacionId(),
                request.entrada(),
                request.salida(),
                request.numeroHuespedes(),
                request.notas()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservaHabitacionResponse.fromReserva(reserva));
    }

    // ===== Mutaciones =====

    @PostMapping("/add")
    @Operation(summary = "Crea una nueva reserva (queda en estado PENDIENTE)")
    public Reserva add(@RequestBody Reserva reserva) {
        if (reserva.getId() == null || reserva.getId().isBlank()) {
            reserva.setId(UUID.randomUUID().toString());
        }
        if (reserva.getCodigo() == null || reserva.getCodigo().isBlank()) {
            reserva.setCodigo("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        return service.crear(reserva);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una reserva existente")
    public Reserva update(@RequestBody Reserva reserva) {
        return service.actualizar(reserva);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una reserva (solo si está PENDIENTE)")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }

    @PostMapping("/{id}/confirmar")
    @Operation(summary = "Confirma una reserva pendiente")
    public void confirmar(@PathVariable String id) {
        service.confirmarReserva(id);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancela una reserva (no completada)")
    public void cancelar(@PathVariable String id) {
        service.cancelarReserva(id);
    }

    @PostMapping("/{id}/completar")
    @Operation(summary = "Completa una reserva confirmada")
    public void completar(@PathVariable String id) {
        service.completarReserva(id);
    }
}