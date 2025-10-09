package com.aponia.aponia_hotel.controller.servicios;

import com.aponia.aponia_hotel.controller.servicios.dto.ServicioDTO;
import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.aponia.aponia_hotel.service.servicios.ServicioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/servicios")
@CrossOrigin(origins = "http://localhost:4200")
public class ServicioRestController {

    private final ServicioService service;

    public ServicioRestController(ServicioService service) {
        this.service = service;
    }

    //@GetMapping("/all")
    //@Operation(summary = "Lista todos los servicios")
    //public List<Servicio> findAll() {
    //    return service.listar();
    //}

    @GetMapping("/all")
    public List<ServicioDTO> listarServicios() {
    return service.listar().stream()
        .map(s -> new ServicioDTO(
            s.getId(),
            s.getNombre(),
            s.getDescripcion(),
            s.getLugar(),
            s.getPrecioPorPersona(),
            s.getDuracionMinutos(),
            s.getCapacidadMaxima(),
            s.getImagenes().stream()
                .map(Imagen::getUrl)
                .toList()
        ))
        .toList();
    }


    @GetMapping("/find/{id}")
    @Operation(summary = "Obtiene un servicio por ID")
    public Servicio findOne(@PathVariable String id) {
        return service.obtener(id).orElse(null);
    }

    @PostMapping("/add")
    @Operation(summary = "Crea un nuevo servicio")
    public void add(@RequestBody Servicio servicio) {
        if (servicio.getId() == null || servicio.getId().isBlank()) {
            servicio.setId(UUID.randomUUID().toString());
        }
        service.crear(servicio);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un servicio existente")
    public void update(@RequestBody Servicio servicio) {
        service.actualizar(servicio);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Elimina un servicio por ID")
    public void delete(@PathVariable String id) {
        service.eliminar(id);
    }
}