package com.aponia.aponia_hotel.controller.resources;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.aponia.aponia_hotel.service.resources.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/imagenes")
@CrossOrigin(origins = "http://localhost:8083")
public class ImagenRestController {

    private final ImagenService service;

    public ImagenRestController(ImagenService service) {
        this.service = service;
    }

    // ===== Lecturas =====

    @GetMapping("/all")
    @Operation(summary = "Lista todas las imágenes")
    public List<Imagen> findAll() {
        return service.listar();
    }

    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene una imagen por ID")
    public Imagen findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    @GetMapping("/servicio/{servicioId}")
    @Operation(summary = "Lista imágenes asociadas a un servicio")
    public List<Imagen> findByServicio(@PathVariable String servicioId) {
        return service.listarPorServicio(servicioId);
    }

    @GetMapping("/tipo-habitacion/{tipoHabitacionId}")
    @Operation(summary = "Lista imágenes asociadas a un tipo de habitación")
    public List<Imagen> findByTipoHabitacion(@PathVariable String tipoHabitacionId) {
        return service.listarPorTipoHabitacion(tipoHabitacionId);
    }

    // ===== Mutaciones (estilo del video: void + @RequestBody) =====
    // Regla: exactamente UNA asociación (servicioId XOR tipoHabitacionId)

    @PostMapping("/add")
    @Operation(summary = "Crea una nueva imagen (asociada a servicio O a tipo de habitación)")
    public void add(@RequestBody Imagen imagen,
                    @RequestParam(value = "servicioId", required = false) String servicioId,
                    @RequestParam(value = "tipoHabitacionId", required = false) String tipoHabitacionId) {

        if (imagen.getId() == null || imagen.getId().isBlank()) {
            imagen.setId(UUID.randomUUID().toString());
        }

        // Si vienen IDs por querystring, construimos referencias livianas
        if (servicioId != null && !servicioId.isBlank()) {
            Servicio ref = new Servicio();
            ref.setId(servicioId);
            imagen.setServicio(ref);
            imagen.setTipoHabitacion(null);
        }
        if (tipoHabitacionId != null && !tipoHabitacionId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoHabitacionId);
            imagen.setTipoHabitacion(ref);
            imagen.setServicio(null);
        }

        // Validación XOR rápida antes de persistir (la entidad también lo valida)
        boolean hasServicio = imagen.getServicio() != null;
        boolean hasTipo = imagen.getTipoHabitacion() != null;
        if (hasServicio == hasTipo) {
            throw new IllegalStateException("Debe asociarse exactamente a servicio O a tipoHabitacion (no ambos/ninguno).");
        }

        service.crear(imagen);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza una imagen (manteniendo una sola asociación)")
    public void update(@RequestBody Imagen imagen,
                       @RequestParam(value = "servicioId", required = false) String servicioId,
                       @RequestParam(value = "tipoHabitacionId", required = false) String tipoHabitacionId) {

        if (servicioId != null && !servicioId.isBlank()) {
            Servicio ref = new Servicio();
            ref.setId(servicioId);
            imagen.setServicio(ref);
            imagen.setTipoHabitacion(null);
        }
        if (tipoHabitacionId != null && !tipoHabitacionId.isBlank()) {
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoHabitacionId);
            imagen.setTipoHabitacion(ref);
            imagen.setServicio(null);
        }

        boolean hasServicio = imagen.getServicio() != null;
        boolean hasTipo = imagen.getTipoHabitacion() != null;
        if (hasServicio == hasTipo) {
            throw new IllegalStateException("Debe asociarse exactamente a servicio O a tipoHabitacion (no ambos/ninguno).");
        }

        service.actualizar(imagen);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina una imagen por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }
}