package com.aponia.aponia_hotel.controller.reservas;

import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.reservas.EstanciaService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estancias")
@CrossOrigin(origins = "http://localhost:4200")
public class EstanciaRestController {

    private final EstanciaService service;

    public EstanciaRestController(EstanciaService service) {
        this.service = service;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todas las estancias")
    public List<Estancia> findAll() {
        return service.listar();
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Lista estancias por reserva")
    public List<Estancia> findByReserva(@PathVariable String reservaId) {
        return service.listarPorReserva(reservaId);
    }

    @GetMapping("/checkins")
    @Operation(summary = "Lista estancias con check-in en la fecha dada y tipo de habitación")
    public List<Estancia> listarCheckinsDelDia(
            @RequestParam String tipoHabitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.listarCheckinsDelDia(tipoHabitacionId, fecha);
    }

    @GetMapping("/checkouts")
    @Operation(summary = "Lista estancias con check-out en la fecha dada y tipo de habitación")
    public List<Estancia> listarCheckoutsDelDia(
            @RequestParam String tipoHabitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.listarCheckoutsDelDia(tipoHabitacionId, fecha);
    }

    @GetMapping("/disponible")
    @Operation(summary = "Verifica disponibilidad para un tipo y rango de fechas")
    public boolean verificarDisponibilidad(
            @RequestParam String tipoHabitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int numeroHuespedes) {
        return service.verificarDisponibilidad(tipoHabitacionId, checkIn, checkOut, numeroHuespedes);
    }

    @GetMapping("/ocupadas/contar")
    @Operation(summary = "Cuenta habitaciones ocupadas de un tipo en un rango")
    public long contarOcupadas(
            @RequestParam String tipoHabitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.contarHabitacionesOcupadas(tipoHabitacionId, checkIn, checkOut);
    }

    @GetMapping("/conflictos")
    @Operation(summary = "Busca estancias que se solapan en una habitación")
    public List<Estancia> conflictos(
            @RequestParam String habitacionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.buscarConflictosFechas(habitacionId, checkIn, checkOut);
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una estancia por ID")
    public Estancia findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    // ===== Mutaciones (estilo video: void / objeto simple) =====

    @PostMapping("/add")
    @Operation(summary = "Crea una nueva estancia")
    public Estancia add(@RequestBody Estancia estancia,
                        @RequestParam(required = false) String tipoHabitacionId) {
        if (estancia.getId() == null || estancia.getId().isBlank()) {
            estancia.setId(UUID.randomUUID().toString());
        }
        if (tipoHabitacionId != null && !tipoHabitacionId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoHabitacionId);
            estancia.setTipoHabitacion(ref);
        }
        return service.crear(estancia);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una estancia existente")
    public Estancia update(@RequestBody Estancia estancia,
                           @RequestParam(required = false) String tipoHabitacionId) {
        if (tipoHabitacionId != null && !tipoHabitacionId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoHabitacionId);
            estancia.setTipoHabitacion(ref);
        }
        return service.actualizar(estancia);
    }

    @PostMapping("/{id}/asignar-habitacion")
    @Operation(summary = "Asigna una habitación disponible a la estancia")
    public Estancia asignarHabitacion(@PathVariable String id) {
        return service.asignarHabitacion(id);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una estancia por ID (solo si la reserva no está COMPLETADA)")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }
}