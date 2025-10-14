package com.aponia.aponia_hotel.controller.servicios;

import com.aponia.aponia_hotel.entities.servicios.ServicioDisponibilidad;
import com.aponia.aponia_hotel.service.servicios.ServicioDisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/disponibilidades")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ServicioDisponibilidadRestController {

    private final ServicioDisponibilidadService service;

    public ServicioDisponibilidadRestController(ServicioDisponibilidadService service) {
        this.service = service;
    }

    // ============================
    // ======= LECTURAS GET =======
    // ============================

    @GetMapping
    @Operation(summary = "Lista todas las disponibilidades")
    public ResponseEntity<List<ServicioDisponibilidad>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una disponibilidad por ID")
    public ResponseEntity<ServicioDisponibilidad> obtener(@PathVariable String id) {
        return service.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/servicio/{servicioId}/fecha/{fecha}")
    @Operation(summary = "Lista disponibilidades de un servicio en una fecha con capacidad mínima")
    public ResponseEntity<List<ServicioDisponibilidad>> listarDisponibles(
            @PathVariable String servicioId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "capacidad", defaultValue = "1") int capacidadRequerida) {
        return ResponseEntity.ok(service.listarDisponibles(servicioId, fecha, capacidadRequerida));
    }

    @GetMapping("/servicio/{servicioId}/rango")
    @Operation(summary = "Lista disponibilidades de un servicio en un rango de fechas")
    public ResponseEntity<List<ServicioDisponibilidad>> listarPorRango(
            @PathVariable String servicioId,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(service.listarPorRangoFechas(servicioId, fechaInicio, fechaFin));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca una disponibilidad específica")
    public ResponseEntity<ServicioDisponibilidad> buscarDisponibilidad(
            @RequestParam String servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio) {
        Optional<ServicioDisponibilidad> r = service.buscarDisponibilidad(servicioId, fecha, horaInicio);
        return r.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/existe")
    @Operation(summary = "Verifica si existe disponibilidad en (servicio, fecha, horaInicio)")
    public ResponseEntity<Boolean> existeDisponibilidad(
            @RequestParam String servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio) {
        return ResponseEntity.ok(service.existeDisponibilidad(servicioId, fecha, horaInicio));
    }

    // ============================
    // ======= MUTACIONES =========
    // ============================

    @PostMapping
    @Operation(summary = "Crea una nueva disponibilidad")
    public ResponseEntity<ServicioDisponibilidad> crear(@RequestBody ServicioDisponibilidad disponibilidad) {
        if (disponibilidad.getId() == null || disponibilidad.getId().isBlank()) {
            disponibilidad.setId(UUID.randomUUID().toString());
        }
        ServicioDisponibilidad creada = service.crear(disponibilidad);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una disponibilidad existente")
    public ResponseEntity<Void> actualizar(@PathVariable String id, @RequestBody ServicioDisponibilidad disponibilidad) {
        disponibilidad.setId(id);
        service.actualizar(disponibilidad);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una disponibilidad por ID")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
