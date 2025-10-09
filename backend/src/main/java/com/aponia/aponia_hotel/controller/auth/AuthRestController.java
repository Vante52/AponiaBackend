package com.aponia.aponia_hotel.controller.auth;


import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.auth.RegistroAppService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8083")
public class AuthRestController {

    private final UsuarioService usuarioService;
    private final RegistroAppService registroAppService;

    public AuthRestController(UsuarioService usuarioService, RegistroAppService registroAppService) {
        this.usuarioService = usuarioService;
        this.registroAppService = registroAppService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login basado en sesión (temporal, igual a MVC)")
    public Map<String,Object> login(@RequestBody Map<String,String> body, HttpSession session) {
        String email = body.get("email");
        String password = body.get("password");
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isPresent() && opt.get().getPasswordHash() != null
                && opt.get().getPasswordHash().equals(password)) { // igual que tu MVC
            Usuario u = opt.get();
            session.setAttribute("AUTH_USER_ID", u.getId());
            session.setAttribute("AUTH_USER_EMAIL", u.getEmail());
            session.setAttribute("AUTH_USER_ROLE", u.getRol());
            return Map.of("ok", true, "rol", u.getRol().name());
        }
        return Map.of("ok", false, "error", "Credenciales inválidas");
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierre de sesión")
    public Map<String,Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    @PostMapping("/register")
    @Operation(summary = "Registro de cliente (y auto-login)")
    public Map<String,Object> register(@RequestBody Map<String,String> body, HttpSession session) {
        Usuario u = registroAppService.registrarCliente(
                body.get("email"), body.get("password"), body.get("nombreCompleto"), body.get("telefono"));
        session.setAttribute("AUTH_USER_ID", u.getId());
        session.setAttribute("AUTH_USER_EMAIL", u.getEmail());
        session.setAttribute("AUTH_USER_ROLE", u.getRol());
        return Map.of("ok", true, "id", u.getId());
    }

    @PostMapping("/password")
    @Operation(summary = "Cambio de contraseña (mismo criterio que MVC)")
    public Map<String,Object> changePassword(@RequestBody Map<String,String> body, HttpSession session) {
        String userId = (String) session.getAttribute("AUTH_USER_ID");
        if (userId == null) return Map.of("ok", false, "error", "No autenticado");
        Optional<Usuario> optUser = usuarioService.obtener(userId);
        if (optUser.isEmpty()) return Map.of("ok", false, "error", "Usuario no encontrado");

        Usuario user = optUser.get();
        if (!user.getPasswordHash().equals(body.get("currentPassword")))
            return Map.of("ok", false, "error", "Contraseña actual incorrecta");
        if (!body.get("newPassword").equals(body.get("confirmPassword")))
            return Map.of("ok", false, "error", "Confirmación no coincide");

        user.setPasswordHash(body.get("newPassword"));
        usuarioService.actualizar(user);
        return Map.of("ok", true);
    }
}