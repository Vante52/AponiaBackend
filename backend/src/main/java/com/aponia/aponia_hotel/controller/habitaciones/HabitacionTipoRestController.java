package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionTipoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import com.aponia.aponia_hotel.controller.habitaciones.dto.HabitacionTipoDTO;
import com.aponia.aponia_hotel.entities.resources.Imagen;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/habitaciones-tipos")
@CrossOrigin(origins = "http://localhost:4200")
public class HabitacionTipoRestController {

    private final HabitacionTipoService service;
    private final HabitacionService habitacionService;

    public HabitacionTipoRestController(HabitacionTipoService service,
                                        HabitacionService habitacionService) {
        this.service = service;
        this.habitacionService = habitacionService;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todos los tipos de habitación")
    public List<HabitacionTipoDTO> listar() {
        return service.listar().stream()
            .map(t -> new HabitacionTipoDTO(
                t.getId(),
                t.getNombre(),
                t.getDescripcion(),
                t.getAforoMaximo(),
                t.getPrecioPorNoche(),
                t.getActiva(),
                t.getImagenes().stream()
                    .map(Imagen::getUrl)
                    .toList()
            ))
            .toList();
    }

    @GetMapping("/activos")
    @Operation(summary = "Lista tipos de habitación activos")
    public List<HabitacionTipo> findActivos() {
        return service.listarActivos();
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene un tipo de habitación por ID")
    public HabitacionTipo findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    // ===== Mutaciones =====

    @PostMapping("/add")
    @Operation(summary = "Crea un nuevo tipo de habitación")
    public void add(@RequestBody HabitacionTipo tipo) {
        if ((tipo.getId() == null) || tipo.getId().isBlank()) {
            tipo.setId(UUID.randomUUID().toString());
        }
        service.crear(tipo);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un tipo de habitación existente")
    public void update(@RequestBody HabitacionTipo tipo) {
        service.actualizar(tipo);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un tipo de habitación por ID (si no tiene habitaciones asociadas)")
    public void delete(@PathVariable String id) {
        // Mismo criterio que en tu MVC: no borrar si hay habitaciones del tipo
        if (!habitacionService.listarPorTipo(id).isEmpty()) {
            throw new IllegalStateException("No se puede eliminar: existen habitaciones asociadas a este tipo");
        }
        service.eliminar(id);
    }
}