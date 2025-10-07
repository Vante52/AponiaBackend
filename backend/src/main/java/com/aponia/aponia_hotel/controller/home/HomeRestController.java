package com.aponia.aponia_hotel.controller.home;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.ClientePerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/home")
@CrossOrigin(origins = "http://localhost:4200")
public class HomeRestController {

    private final UsuarioService usuarioService;
    private final ClientePerfilService clientePerfilService;

    public HomeRestController(UsuarioService usuarioService, ClientePerfilService clientePerfilService) {
        this.usuarioService = usuarioService;
        this.clientePerfilService = clientePerfilService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Datos básicos para dashboard del usuario autenticado")
    public Object dashboard(HttpSession session) {
        String email = (String) session.getAttribute("AUTH_USER_EMAIL");
        if (email == null) return Map.of("ok", false, "error", "No autenticado");
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) return Map.of("ok", false, "error", "Usuario no encontrado");
        Usuario u = opt.get();
        ClientePerfil perfil = clientePerfilService.obtener(u.getId()).orElse(null);
        return Map.of("ok", true, "usuario", u, "clientePerfil", perfil);
    }

    @PutMapping("/user_info/{email}")
    @Operation(summary = "Actualizar perfil del usuario (nombre, teléfono, etc.)")
    public Map<String,Object> updateUserInfo(@PathVariable String email, @RequestBody ClientePerfil form) {
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) return Map.of("ok", false, "error", "Usuario no encontrado");
        var u = opt.get();
        var perfilOpt = clientePerfilService.obtener(u.getId());
        if (perfilOpt.isEmpty()) return Map.of("ok", false, "error", "Perfil no encontrado");
        var perfil = perfilOpt.get();
        perfil.setNombreCompleto(form.getNombreCompleto());
        perfil.setTelefono(form.getTelefono());
        clientePerfilService.actualizar(perfil);
        return Map.of("ok", true);
    }

    @DeleteMapping("/user_info/{email}")
    @Operation(summary = "Eliminar usuario y perfil")
    public Map<String,Object> deleteUser(@PathVariable String email, HttpSession session) {
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) return Map.of("ok", false, "error", "Usuario no encontrado");
        var u = opt.get();
        usuarioService.eliminar(u.getId());
        clientePerfilService.eliminar(u.getId());
        session.invalidate();
        return Map.of("ok", true);
    }
}