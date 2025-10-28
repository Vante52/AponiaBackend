package com.aponia.aponia_hotel.controller.reservas;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.ReservaServicio;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.aponia.aponia_hotel.service.reservas.ReservaServicioService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/reservas-servicios")
@CrossOrigin(origins = "http://localhost:4200")
public class ReservaServicioRestController {

    private final ReservaServicioService service;

    public ReservaServicioRestController(ReservaServicioService service) {
        this.service = service;
    }

    // ===== Lecturas =====
    @GetMapping("/all")
    @Operation(summary = "Lista todas las reservas de servicios")
    public List<ReservaServicio> findAll() {
        return service.listar();
    }

    @GetMapping("/reserva/{reservaId}")
    @Operation(summary = "Lista reservas de servicios por reserva")
    public List<ReservaServicio> porReserva(@PathVariable String reservaId) {
        return service.findByReservaId(reservaId);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Lista reservas de un servicio en una fecha")
    public List<ReservaServicio> porServicioYFecha(
            @PathVariable String servicioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.findByServicioIdAndFecha(servicioId, fecha);
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una reserva de servicio por ID")
    public ReservaServicio findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    // ===== Mutaciones =====
    @PostMapping("/add")
    @Operation(summary = "Crea una reserva de servicio")
    public void add(@RequestBody ReservaServicio rs,
            @RequestParam String reservaId,
            @RequestParam String servicioId) {
        if (rs.getId() == null || rs.getId().isBlank()) {
            rs.setId(UUID.randomUUID().toString());
        }
        // referencias livianas
        Reserva r = new Reserva();
        r.setId(reservaId);
        rs.setReserva(r);
        Servicio s = new Servicio();
        s.setId(servicioId);
        rs.setServicio(s);

        service.crear(rs);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una reserva de servicio")
    public void update(@RequestBody ReservaServicio rs,
            @RequestParam(required = false) String reservaId,
            @RequestParam(required = false) String servicioId) {
        if (reservaId != null && !reservaId.isBlank()) {
            Reserva r = new Reserva();
            r.setId(reservaId);
            rs.setReserva(r);
        }
        if (servicioId != null && !servicioId.isBlank()) {
            Servicio s = new Servicio();
            s.setId(servicioId);
            rs.setServicio(s);
        }
        service.actualizar(rs);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una reserva de servicio por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }

    @GetMapping("/{id}/servicio-id")
    @Operation(summary = "Obtiene el ID del servicio para una reserva de servicio")
    public String getServicioId(@PathVariable String id) {
        return service.obtener(id)
                .map(reservaServicio -> reservaServicio.getServicio().getId())
                .orElse(null);
    }
}
