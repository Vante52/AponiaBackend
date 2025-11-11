package com.aponia.aponia_hotel.controller.admin;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.security.jwt.UsuarioPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminRestController {

    @GetMapping("/can-access")
    @Operation(summary = "Verifica si el usuario en sesión es ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String,Object> canAccess(@AuthenticationPrincipal UsuarioPrincipal principal) {
        boolean ok = principal != null && principal.getRol() == Usuario.UserRole.ADMIN;
        return ok ? Map.of("ok", true) : Map.of("ok", false, "error", "No autorizado");
    }
}
