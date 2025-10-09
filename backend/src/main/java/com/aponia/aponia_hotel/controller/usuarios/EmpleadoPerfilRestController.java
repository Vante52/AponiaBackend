package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.EmpleadoPerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados-perfil")
@CrossOrigin(origins = "http://localhost:8083")
@Tag(name = "Perfiles de Empleado", description = "CRUD de perfiles de empleados")
public class EmpleadoPerfilRestController {

    private final EmpleadoPerfilService service;
    private final UsuarioService usuarioService;

    public EmpleadoPerfilRestController(EmpleadoPerfilService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todos los perfiles de empleado")
    public List<EmpleadoPerfil> listar() { return service.listar(); }

    @GetMapping("/find/{usuarioId}")
    @Operation(summary = "Obtiene un perfil de empleado por usuarioId")
    public EmpleadoPerfil findOne(@PathVariable String usuarioId) {
        return service.obtener(usuarioId).orElse(null);
    }

    @PostMapping("/add")
    @Operation(summary = "Crea un perfil de empleado (MapsId)")
    public EmpleadoPerfil add(@RequestParam String usuarioId, @RequestBody EmpleadoPerfil perfil) {
        perfil.setUsuarioId(usuarioId);
        Usuario u = usuarioService.obtener(usuarioId).orElseThrow();
        perfil.setUsuario(u);
        return service.crear(perfil);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un perfil de empleado")
    public EmpleadoPerfil update(@RequestBody EmpleadoPerfil perfil) {
        return service.actualizar(perfil);
    }

    @DeleteMapping("/delete/{usuarioId}")
    @Operation(summary = "Elimina un perfil de empleado")
    public void delete(@PathVariable String usuarioId) {
        service.eliminar(usuarioId);
    }
}