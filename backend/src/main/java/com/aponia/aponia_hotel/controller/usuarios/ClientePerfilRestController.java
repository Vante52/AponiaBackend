package com.aponia.aponia_hotel.controller.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.ClientePerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.server.ResponseStatusException;
    import java.util.List;

@RestController
@RequestMapping("/api/clientes-perfil")
@CrossOrigin(origins = "http://localhost:8083")
@Tag(name = "Perfiles de Cliente", description = "CRUD de perfiles de clientes")
public class ClientePerfilRestController {

    private final ClientePerfilService service;
    private final UsuarioService usuarioService;

    public ClientePerfilRestController(ClientePerfilService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todos los perfiles de cliente")
    public List<ClientePerfil> listar() { return service.listar(); }

    @GetMapping("/find/{usuarioId}")
    @Operation(summary = "Obtiene un perfil de cliente por usuarioId")
    public ClientePerfil findOne(@PathVariable String usuarioId) {
        return service.obtener(usuarioId).orElse(null);
    }

    @PostMapping("/add")
    @Operation(summary = "Crea un perfil de cliente (MapsId)")
    public ClientePerfil add(@RequestParam String usuarioId, @RequestBody ClientePerfil perfil) {
        Usuario usuario = usuarioService.obtener(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));
        if (service.obtener(usuarioId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El perfil de cliente ya existe para este usuario");
        }
        perfil.setUsuarioId(usuarioId);
        perfil.setUsuario(usuario);
        return service.crear(perfil);
    }

    @PutMapping("/update")
    @Operation(summary = "Actualiza un perfil de cliente")
    public ClientePerfil update(@RequestBody ClientePerfil perfil) {
        String usuarioId = perfil.getUsuarioId();
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuarioId es obligatorio");
        }
        ClientePerfil existente = service.obtener(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El perfil de cliente no existe"));
        // copiar campos actualizados
        existente.setNombreCompleto(perfil.getNombreCompleto());
        existente.setTelefono(perfil.getTelefono());
        // aquí puedes copiar otros campos opcionales (dirección, etc.)
        return service.actualizar(existente);
    }

    @DeleteMapping("/delete/{usuarioId}")
    @Operation(summary = "Elimina un perfil de cliente")
    public void delete(@PathVariable String usuarioId) {
        service.eliminar(usuarioId);
    }
}
