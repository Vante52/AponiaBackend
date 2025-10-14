package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.controller.habitaciones.dto.HabitacionDTO;
import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habitaciones")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class HabitacionRestController {

    private final HabitacionService service;

    public HabitacionRestController(HabitacionService service) {
        this.service = service;
    }

    // ===============================
    // ======= LECTURAS GET ==========
    // ===============================

    @GetMapping
    @Operation(summary = "Lista todas las habitaciones con su tipo")
    public ResponseEntity<List<HabitacionDTO>> listar() {
        var lista = service.listar().stream()
                .map(HabitacionDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/activos")
    @Operation(summary = "Lista habitaciones activas")
    public ResponseEntity<List<Habitacion>> listarActivas() {
        return ResponseEntity.ok(service.listarActivas());
    }

    @GetMapping("/tipo/{tipoId}")
    @Operation(summary = "Lista habitaciones por tipo de habitación")
    public ResponseEntity<List<Habitacion>> listarPorTipo(@PathVariable String tipoId) {
        return ResponseEntity.ok(service.listarPorTipo(tipoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una habitación por ID con su tipo")
    public ResponseEntity<HabitacionDTO> obtener(@PathVariable String id) {
        return service.obtener(id)
                .map(h -> ResponseEntity.ok(HabitacionDTO.fromEntity(h)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ===============================
    // ======= MUTACIONES ============
    // ===============================

    @PostMapping
    @Operation(summary = "Crea una nueva habitación")
    public ResponseEntity<Habitacion> crear(@RequestBody Habitacion habitacion) {
        if (habitacion.getId() == null || habitacion.getId().isBlank()) {
            habitacion.setId(UUID.randomUUID().toString());
        }

        if (habitacion.getTipo() == null || habitacion.getTipo().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Habitacion creada = service.crear(habitacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una habitación existente")
    public ResponseEntity<Void> actualizar(@PathVariable String id, @RequestBody Habitacion habitacion) {
        habitacion.setId(id);

        if (habitacion.getTipo() == null || habitacion.getTipo().getId() == null) {
            return ResponseEntity.badRequest().build();
        }

        service.actualizar(habitacion);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina una habitación por ID")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
