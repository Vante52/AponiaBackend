
package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.EmpleadoPerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/empleados-perfil")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Perfiles de Empleado", description = "CRUD de perfiles de empleados")
public class EmpleadoPerfilRestController {

    private final EmpleadoPerfilService service;
    private final UsuarioService usuarioService;

    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("9999999999.99");

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
    public EmpleadoPerfil add(@RequestBody EmpleadoPerfil perfil) {
        String usuarioId = perfil.getUsuarioId();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuarioId es obligatorio");
        }

        Usuario usuario = usuarioService.obtener(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));

        if (service.obtener(usuarioId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El perfil de empleado ya existe para este usuario");
        }

        validarSalario(perfil);
        perfil.setUsuario(usuario);
        return service.crear(perfil);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un perfil de empleado")
    public EmpleadoPerfil update(@RequestBody EmpleadoPerfil perfil) {
        String usuarioId = perfil.getUsuarioId();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuarioId es obligatorio");
        }

        EmpleadoPerfil existente = service.obtener(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El perfil de empleado no existe"));

        validarSalario(perfil);
        // copiar los campos cambiados
        existente.setNombreCompleto(perfil.getNombreCompleto());
        existente.setTelefono(perfil.getTelefono());
        existente.setCargo(perfil.getCargo());
        existente.setSalario(perfil.getSalario());
        existente.setFechaContratacion(perfil.getFechaContratacion());

        return service.actualizar(existente);
    }

    private void validarSalario(EmpleadoPerfil perfil) {
        if (perfil.getSalario() != null && perfil.getSalario().compareTo(SALARIO_MAXIMO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El salario no puede superar " + SALARIO_MAXIMO.toPlainString());
        }
    }

    @DeleteMapping("/delete/{usuarioId}")
    @Operation(summary = "Elimina un perfil de empleado")
    public void delete(@PathVariable String usuarioId) {
        service.eliminar(usuarioId);
    }
}
