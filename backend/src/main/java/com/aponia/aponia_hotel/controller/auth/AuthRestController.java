package com.aponia.aponia_hotel.controller.auth;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.security.jwt.JwtTokenService;
import com.aponia.aponia_hotel.security.jwt.UsuarioPrincipal;
import com.aponia.aponia_hotel.service.auth.LoginAppService;
import com.aponia.aponia_hotel.service.auth.RegistroAppService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Autenticación", description = "Endpoints de registro, login y logout")
public class AuthRestController {

    private final RegistroAppService registroAppService;
    private final LoginAppService loginAppService;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthRestController(RegistroAppService registroAppService,
                              LoginAppService loginAppService,
                              UsuarioService usuarioService,
                              PasswordEncoder passwordEncoder,
                              JwtTokenService jwtTokenService) {
        this.registroAppService = registroAppService;
        this.loginAppService = loginAppService;
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    // ============================================================
    // LOGIN
    // ============================================================
    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión con JWT")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            Usuario usuario = loginAppService.login(email, password);

            Map<String, Object> response = buildAuthResponse(usuario);
            response.put("message", "Inicio de sesión exitoso");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE CLIENTE
    // ============================================================
    @PostMapping("/register")
    @Operation(summary = "Registro de nuevo cliente y entrega de JWT")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String nombreCompleto = body.get("nombreCompleto");
            String telefono = body.get("telefono");

            Usuario u = registroAppService.registrarCliente(email, password, nombreCompleto, telefono);

            Map<String, Object> response = buildAuthResponse(u);
            response.put("message", "Registro exitoso");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE ADMIN
    // ============================================================
    @PostMapping("/register-admin")
    @Operation(summary = "Registro de nuevo administrador y entrega de JWT")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String nombreCompleto = body.get("nombreCompleto");
            String telefono = body.get("telefono");

            Usuario u = registroAppService.registrarAdmin(email, password, nombreCompleto, telefono);

            Map<String, Object> response = buildAuthResponse(u);
            response.put("message", "Registro exitoso");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE EMPLEADO (ADMIN USE)
    // ============================================================
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-empleado")
    @Operation(summary = "Registro de nuevo empleado (solo ADMIN)")
    public ResponseEntity<?> registerEmpleado(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String nombreCompleto = body.get("nombreCompleto");
            String telefono = body.get("telefono");
            String cargo = body.get("cargo");
            BigDecimal salario = new BigDecimal(body.getOrDefault("salario", "0"));

            Usuario u = registroAppService.registrarEmpleado(email, password, nombreCompleto, telefono, cargo, salario);

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Empleado registrado exitosamente",
                    "id", u.getId(),
                    "rol", u.getRol().name()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // LOGOUT
    // ============================================================
    @PostMapping("/logout")
    @Operation(summary = "Cierre de sesión lógico en el cliente")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("ok", true, "message", "Sesión cerrada correctamente"));
    }

    // ============================================================
    // PERFIL ACTUAL (/me)
    // ============================================================
    @GetMapping("/me")
    @Operation(summary = "Obtiene la información del usuario actualmente autenticado")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UsuarioPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "error", "No autenticado"));
        }

        return usuarioService.obtener(principal.getId())
                .map(u -> {
                    String nombreCompleto = null;
                    try {
                        if (u.getClientePerfil() != null && u.getClientePerfil().getNombreCompleto() != null) {
                            nombreCompleto = u.getClientePerfil().getNombreCompleto();
                        } else if (u.getEmpleadoPerfil() != null && u.getEmpleadoPerfil().getNombreCompleto() != null) {
                            nombreCompleto = u.getEmpleadoPerfil().getNombreCompleto();
                        }
                    } catch (Exception ignored) {
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("ok", true);
                    data.put("id", u.getId());
                    data.put("email", u.getEmail());
                    data.put("rol", u.getRol().name());
                    data.put("nombreCompleto", nombreCompleto);

                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.status(404)
                        .body(Map.of("ok", false, "error", "Usuario no encontrado")));
    }

    // ============================================================
    // CAMBIO DE CONTRASEÑA
    // ============================================================
    @PostMapping("/password")
    @Operation(summary = "Cambio de contraseña simple")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body,
                                            @AuthenticationPrincipal UsuarioPrincipal principal) {
        if (principal == null)
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "No autenticado"));

        return usuarioService.obtener(principal.getId())
                .map(usuario -> {
                    String current = body.get("currentPassword");
                    String nueva = body.get("newPassword");
                    String confirm = body.get("confirmPassword");

                    if (!passwordEncoder.matches(current, usuario.getPasswordHash()))
                        return ResponseEntity.badRequest()
                                .body(Map.of("ok", false, "error", "Contraseña actual incorrecta"));

                    if (!nueva.equals(confirm))
                        return ResponseEntity.badRequest()
                                .body(Map.of("ok", false, "error", "Las contraseñas no coinciden"));

                    usuario.setPasswordHash(passwordEncoder.encode(nueva));
                    usuarioService.actualizar(usuario);
                    return ResponseEntity.ok(Map.of("ok", true, "message", "Contraseña actualizada"));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Usuario no encontrado")));
    }

    // ============================================================
    // GENERAR RESPUESTA CON JWT
    // ============================================================
    private Map<String, Object> buildAuthResponse(Usuario usuario) {
        String token = jwtTokenService.generateToken(usuario);
        Map<String, Object> response = new HashMap<>();
        response.put("ok", true);
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", jwtTokenService.getExpirationSeconds());
        response.put("id", usuario.getId());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol().name());
        return response;
    }
}
