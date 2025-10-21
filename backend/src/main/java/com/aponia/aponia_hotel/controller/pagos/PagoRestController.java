package com.aponia.aponia_hotel.controller.pagos;

import com.aponia.aponia_hotel.entities.pagos.Pago;
import com.aponia.aponia_hotel.entities.pagos.Pago.EstadoPago;
import com.aponia.aponia_hotel.entities.pagos.Pago.TipoPago;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.service.pagos.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "http://localhost:4200")
public class PagoRestController {

    private final PagoService service;

    public PagoRestController(PagoService service) {
        this.service = service;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todos los pagos")
    public List<Pago> findAll() { return service.listar(); }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene un pago por ID")
    public Pago findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Lista pagos por reserva")
    public List<Pago> porReserva(@PathVariable String reservaId) {
        return service.listarPorReserva(reservaId);
    }

    @GetMapping("/reserva/{reservaId}/estado/{estado}")
    @Operation(summary = "Lista pagos por reserva y estado")
    public List<Pago> porReservaYEstado(@PathVariable String reservaId, @PathVariable EstadoPago estado) {
        return service.listarPorReservaYEstado(reservaId, estado);
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Lista pagos por tipo")
    public List<Pago> porTipo(@PathVariable TipoPago tipo) {
        return service.listarPorTipo(tipo);
    }

    @GetMapping("/reserva/{reservaId}/total-completados")
    @Operation(summary = "Total de pagos COMPLETADOS para una reserva")
    public double totalCompletados(@PathVariable String reservaId) {
        return service.calcularTotalPagosCompletados(reservaId);
    }

    // ===== Mutaciones CRUD =====

    @PostMapping("/add")
    @Operation(summary = "Crea un nuevo pago (COMPLETADO por defecto)")
    public Pago add(@RequestBody Pago pago, @RequestParam String reservaId) {
        if (pago.getId() == null || pago.getId().isBlank()) {
            pago.setId(UUID.randomUUID().toString());
        }
        // referencia liviana a Reserva
        Reserva r = new Reserva(); r.setId(reservaId);
        pago.setReserva(r);
        return service.crear(pago);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un pago existente")
    public Pago update(@RequestBody Pago pago, @RequestParam(required = false) String reservaId) {
        if (reservaId != null && !reservaId.isBlank()) {
            Reserva r = new Reserva(); r.setId(reservaId);
            pago.setReserva(r);
        }
        return service.actualizar(pago);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un pago por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }

    // ===== Acciones de negocio =====

    @PostMapping("/{id}/completar")
    @Operation(summary = "Marca un pago como COMPLETADO")
    public void completar(@PathVariable String id) {
        service.completarPago(id);
    }

    @PostMapping("/{id}/fallido")
    @Operation(summary = "Marca un pago como FALLIDO")
    public void fallido(@PathVariable String id) {
        service.marcarPagoFallido(id);
    }

    @PostMapping("/{id}/reembolso")
    @Operation(summary = "Procesa reembolso de un pago COMPLETADO (crea movimiento de REEMBOLSO)")
    public void reembolso(@PathVariable String id) {
        service.procesarReembolso(id);
    }
}