package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionTipoService;
import com.aponia.aponia_hotel.controller.habitaciones.dto.HabitacionTipoDTO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habitaciones-tipos")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class HabitacionTipoRestController {

    private final HabitacionTipoService service;
    private final HabitacionService habitacionService;

    public HabitacionTipoRestController(HabitacionTipoService service, HabitacionService habitacionService) {
        this.service = service;
        this.habitacionService = habitacionService;
    }

    // ====== GET ALL ======
    @GetMapping
    @Operation(summary = "Lista todos los tipos de habitación")
    public ResponseEntity<List<HabitacionTipoDTO>> listar() {
        var lista = service.listar().stream()
                .map(t -> new HabitacionTipoDTO(
                        t.getId(),
                        t.getNombre(),
                        t.getDescripcion(),
                        t.getAforoMaximo(),
                        t.getPrecioPorNoche(),
                        t.getActiva(),
                        t.getImagenes().stream().map(Imagen::getUrl).toList()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    // ====== GET ACTIVOS ======
    @GetMapping("/activos")
    @Operation(summary = "Lista los tipos de habitación activos")
    public ResponseEntity<List<HabitacionTipo>> listarActivos() {
        return ResponseEntity.ok(service.listarActivos());
    }

    // ====== GET BY ID ======
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un tipo de habitación por ID")
    public ResponseEntity<HabitacionTipo> findById(@PathVariable String id) {
        return service.obtener(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ====== POST (CREATE) ======
    @PostMapping
    @Operation(summary = "Crea un nuevo tipo de habitación")
    public ResponseEntity<HabitacionTipo> create(@RequestBody HabitacionTipo tipo) {
        tipo.setId(UUID.randomUUID().toString());

        if (tipo.getImagenes() != null) {
            tipo.getImagenes().forEach(img -> {
                img.setTipoHabitacion(tipo); // vincula correctamente
            });
        }

        HabitacionTipo creado = service.crear(tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // ====== PUT (UPDATE) ======
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un tipo de habitación existente")
    public ResponseEntity<Void> update(@PathVariable String id, @RequestBody HabitacionTipo tipo) {
        tipo.setId(id);
        service.actualizar(tipo);
        return ResponseEntity.ok().build();
    }

    // ====== DELETE ======
    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un tipo de habitación si no tiene habitaciones asociadas")
    public ResponseEntity<String> delete(@PathVariable String id) {
        if (!habitacionService.listarPorTipo(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede eliminar: hay habitaciones asociadas");
        }
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
