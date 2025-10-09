package com.aponia.aponia_hotel.controller.admin;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8083")
public class AdminRestController {

    @GetMapping("/can-access")
    @Operation(summary = "Verifica si el usuario en sesión es ADMIN")
    public Map<String,Object> canAccess(HttpSession session) {
        Usuario.UserRole rol = (Usuario.UserRole) session.getAttribute("AUTH_USER_ROLE");
        boolean ok = (rol == Usuario.UserRole.ADMIN);
        return ok ? Map.of("ok", true) : Map.of("ok", false, "error", "No autorizado");
    }
}
