package com.aponia.aponia_hotel.controller.servicios;

import com.aponia.aponia_hotel.entities.servicios.ServicioDisponibilidad;
import com.aponia.aponia_hotel.service.servicios.ServicioDisponibilidadService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/disponibilidades")
@CrossOrigin(origins = "http://localhost:4200")
public class ServicioDisponibilidadRestController {

    private final ServicioDisponibilidadService service;

    public ServicioDisponibilidadRestController(ServicioDisponibilidadService service) {
        this.service = service;
    }

    // === Lecturas básicas ===

    @GetMapping("/all")
    @Operation(summary = "Lista todas las disponibilidades")
    public List<ServicioDisponibilidad> findAll() {
        return service.listar();
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una disponibilidad por ID")
    public ServicioDisponibilidad findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    // === Consultas especializadas ===

    @GetMapping("/servicio/{servicioId}/fecha/{fecha}")
    @Operation(summary = "Lista disponibilidades de un servicio en una fecha con capacidad mínima")
    public List<ServicioDisponibilidad> listarDisponibles(
            @PathVariable String servicioId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(name = "capacidad", defaultValue = "1") int capacidadRequerida) {
        return service.listarDisponibles(servicioId, fecha, capacidadRequerida);
    }

    @GetMapping("/servicio/{servicioId}/rango")
    @Operation(summary = "Lista disponibilidades de un servicio en un rango de fechas")
    public List<ServicioDisponibilidad> listarPorRango(
            @PathVariable String servicioId,
            @RequestParam("inicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return service.listarPorRangoFechas(servicioId, fechaInicio, fechaFin);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca una disponibilidad específica (servicio, fecha, horaInicio)")
    public ServicioDisponibilidad buscarDisponibilidad(
            @RequestParam String servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio) {
        Optional<ServicioDisponibilidad> r = service.buscarDisponibilidad(servicioId, fecha, horaInicio);
        return r.orElse(null);
    }

    @GetMapping("/existe")
    @Operation(summary = "Verifica si existe disponibilidad en (servicio, fecha, horaInicio)")
    public boolean existeDisponibilidad(
            @RequestParam String servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio) {
        return service.existeDisponibilidad(servicioId, fecha, horaInicio);
    }

    // === Mutaciones estilo video (void + @RequestBody) ===

    @PostMapping("/add")
    @Operation(summary = "Crea una nueva disponibilidad")
    public void add(@RequestBody ServicioDisponibilidad disponibilidad) {
        if (disponibilidad.getId() == null || disponibilidad.getId().isBlank()) {
            disponibilidad.setId(UUID.randomUUID().toString());
        }
        service.crear(disponibilidad);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una disponibilidad existente")
    public void update(@RequestBody ServicioDisponibilidad disponibilidad) {
        service.actualizar(disponibilidad);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una disponibilidad por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }
}
