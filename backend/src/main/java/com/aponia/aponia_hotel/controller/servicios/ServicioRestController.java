package com.aponia.aponia_hotel.controller.servicios;

import com.aponia.aponia_hotel.controller.servicios.dto.ServicioDTO;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.aponia.aponia_hotel.service.servicios.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ServicioRestController {

    private final ServicioService service;

    public ServicioRestController(ServicioService service) {
        this.service = service;
    }

    // ============================
    // ======= LECTURAS GET =======
    // ============================

    @GetMapping
    @Operation(summary = "Lista todos los servicios con sus imágenes (DTO)")
    public ResponseEntity<List<ServicioDTO>> listarServicios() {
        List<ServicioDTO> lista = service.listar().stream()
                .map(s -> new ServicioDTO(
                        s.getId(),
                        s.getNombre(),
                        s.getDescripcion(),
                        s.getLugar(),
                        s.getPrecioPorPersona(),
                        s.getDuracionMinutos(),
                        s.getCapacidadMaxima(),
                        s.getImagenes() != null
                                ? s.getImagenes().stream().map(Imagen::getUrl).toList()
                                : List.of()
                ))
                .toList();

        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un servicio por ID")
    public ResponseEntity<ServicioDTO> obtener(@PathVariable String id) {
        return service.obtener(id)
                .map(s -> new ServicioDTO(
                        s.getId(),
                        s.getNombre(),
                        s.getDescripcion(),
                        s.getLugar(),
                        s.getPrecioPorPersona(),
                        s.getDuracionMinutos(),
                        s.getCapacidadMaxima(),
                        s.getImagenes() != null
                                ? s.getImagenes().stream().map(Imagen::getUrl).toList()
                                : List.of()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ============================
    // ======= MUTACIONES =========
    // ============================

    @PostMapping
    @Operation(summary = "Crea un nuevo servicio")
    public ResponseEntity<Servicio> crear(@RequestBody Servicio servicio) {
        if (servicio.getId() == null || servicio.getId().isBlank()) {
            servicio.setId(UUID.randomUUID().toString());
        }
        Servicio creado = service.crear(servicio);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un servicio existente")
    public ResponseEntity<Void> actualizar(@PathVariable String id, @RequestBody Servicio servicio) {
        servicio.setId(id);
        service.actualizar(servicio);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un servicio por ID")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
