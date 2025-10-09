package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habitaciones")
@CrossOrigin(origins = "http://localhost:8083")
public class HabitacionRestController {

    private final HabitacionService service;

    public HabitacionRestController(HabitacionService service) {
        this.service = service;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todas las habitaciones")
    public List<Habitacion> findAll() {
        return service.listar();
    }

    @GetMapping("/activos")
    @Operation(summary = "Lista habitaciones activas")
    public List<Habitacion> findActivas() {
        return service.listarActivas();
    }

    @GetMapping("/tipo/{tipoId}")
    @Operation(summary = "Lista habitaciones activas por tipo")
    public List<Habitacion> findByTipo(@PathVariable String tipoId) {
        return service.listarPorTipo(tipoId);
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una habitación por ID")
    public Habitacion findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    // ===== Mutaciones estilo video (void + @RequestBody) =====

    @PostMapping("/add")
    @Operation(summary = "Crea una nueva habitación")
    public void add(@RequestBody Habitacion habitacion,
                    @RequestParam(value = "tipoId", required = false) String tipoId) {

        if ((habitacion.getId() == null) || habitacion.getId().isBlank()) {
            habitacion.setId(UUID.randomUUID().toString());
        }
        // Ayudamos al front: si nos mandan tipoId por querystring, armamos la referencia
        if (tipoId != null && !tipoId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoId);
            habitacion.setTipo(ref);
        }
        service.crear(habitacion);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una habitación existente")
    public void update(@RequestBody Habitacion habitacion,
                       @RequestParam(value = "tipoId", required = false) String tipoId) {
        if (tipoId != null && !tipoId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoId);
            habitacion.setTipo(ref);
        }
        service.actualizar(habitacion);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una habitación por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }
}