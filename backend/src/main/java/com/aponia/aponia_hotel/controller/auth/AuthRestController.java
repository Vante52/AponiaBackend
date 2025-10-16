package com.aponia.aponia_hotel.controller.auth;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.auth.LoginAppService;
import com.aponia.aponia_hotel.service.auth.RegistroAppService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST que encapsula los flujos de autenticación y registro.
 * Se dejan documentadas las decisiones claves para facilitar el mantenimiento
 * por parte de cualquier estudiante que continúe con el proyecto.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Autenticación", description = "Endpoints de registro, login y logout")
public class AuthRestController {

    private static final String SESSION_USER_ID = "AUTH_USER_ID";
    private static final String SESSION_USER_EMAIL = "AUTH_USER_EMAIL";
    private static final String SESSION_USER_ROLE = "AUTH_USER_ROLE";

    private final RegistroAppService registroAppService;
    private final LoginAppService loginAppService;
    private final UsuarioService usuarioService;

    public AuthRestController(RegistroAppService registroAppService,
            LoginAppService loginAppService,
            UsuarioService usuarioService) {
        this.registroAppService = registroAppService;
        this.loginAppService = loginAppService;
        this.usuarioService = usuarioService;
    }

    // ============================================================
    // LOGIN
    // ============================================================
    /**
     * Inicia una sesión basada en {@link HttpSession}. Se almacena la información mínima
     * necesaria para identificar al usuario en llamadas posteriores.
     */
    @PostMapping("/login")
    @Operation(summary = "Inicio de sesión simple con sesión HTTP")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            Usuario usuario = loginAppService.login(email, password);

            // Guardamos en sesión HTTP (temporal)
            session.setAttribute(SESSION_USER_ID, usuario.getId());
            session.setAttribute(SESSION_USER_EMAIL, usuario.getEmail());
            session.setAttribute(SESSION_USER_ROLE, usuario.getRol().name());

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Inicio de sesión exitoso",
                    "id", usuario.getId(),
                    "email", usuario.getEmail(),
                    "rol", usuario.getRol().name()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE CLIENTE
    // ============================================================
    /**
     * Registra un nuevo cliente y abre una sesión en el mismo flujo para mejorar
     * la experiencia de incorporación.
     */
    @PostMapping("/register")
    @Operation(summary = "Registro de nuevo cliente y creación de sesión HTTP")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String nombreCompleto = body.get("nombreCompleto");
            String telefono = body.get("telefono");

            Usuario u = registroAppService.registrarCliente(email, password, nombreCompleto, telefono);

            session.setAttribute(SESSION_USER_ID, u.getId());
            session.setAttribute(SESSION_USER_EMAIL, u.getEmail());
            session.setAttribute(SESSION_USER_ROLE, u.getRol().name());

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Registro exitoso",
                    "id", u.getId(),
                    "rol", u.getRol().name()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE ADMIN
    // ============================================================
    /**
     * Permite crear un nuevo usuario con rol administrativo. Se reutiliza la lógica
     * del servicio de registro pero se mantiene separado para dejar clara la intención.
     */
    @PostMapping("/register-admin")
    @Operation(summary = "Registro de nuevo cliente y creación de sesión HTTP")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String email = body.get("email");
            String password = body.get("password");
            String nombreCompleto = body.get("nombreCompleto");
            String telefono = body.get("telefono");

            Usuario u = registroAppService.registrarAdmin(email, password, nombreCompleto, telefono);

            session.setAttribute(SESSION_USER_ID, u.getId());
            session.setAttribute(SESSION_USER_EMAIL, u.getEmail());
            session.setAttribute(SESSION_USER_ROLE, u.getRol().name());

            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "message", "Registro exitoso",
                    "id", u.getId(),
                    "rol", u.getRol().name()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", e.getMessage()));
        }
    }

    // ============================================================
    // REGISTRO DE EMPLEADO (ADMIN USE)
    // ============================================================
    /**
     * Alta de personal operativo. Esta operación está pensada para ser invocada únicamente
     * por personal administrador desde el panel de gestión.
     */
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
    /**
     * Cierra la sesión activa y libera los atributos almacenados en el servidor.
     */
    @PostMapping("/logout")
    @Operation(summary = "Cierre de sesión HTTP")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("ok", true, "message", "Sesión cerrada correctamente"));
    }

    // ============================================================
    // OBTENER USUARIO ACTUAL (por sesión HTTP)
    // ============================================================
    // ============================================================
    // ============================================================
    // PERFIL ACTUAL (/me)
    // ============================================================
    /**
     * Permite al frontend conocer los detalles básicos del usuario autenticado.
     * Se devuelve siempre el mismo esquema para que la interfaz degrade correctamente
     * en caso de que la sesión haya expirado o nunca se haya iniciado.
     */
    @GetMapping("/me")
    @Operation(summary = "Obtiene la información del usuario actualmente autenticado")
    public ResponseEntity<Map<String, Object>> me(HttpSession session) {
        String userId = (String) session.getAttribute(SESSION_USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "error", "No autenticado"));
        }

        return usuarioService.obtener(userId)
                .map(u -> {
                    String nombreCompleto = extraerNombreCompleto(u);

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
    /**
     * Permite a la persona usuaria actualizar su contraseña desde la sesión actual.
     * Se validan los datos críticos y se devuelven mensajes claros para facilitar el
     * rastreo de errores desde el cliente.
     */
    @PostMapping("/password")
    @Operation(summary = "Cambio de contraseña simple")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpSession session) {
        String userId = (String) session.getAttribute(SESSION_USER_ID);
        if (userId == null)
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "No autenticado"));

        return usuarioService.obtener(userId)
                .map(usuario -> {
                    String current = body.get("currentPassword");
                    String nueva = body.get("newPassword");
                    String confirm = body.get("confirmPassword");

                    if (!usuario.getPasswordHash().equals(current))
                        return ResponseEntity.badRequest()
                                .body(Map.of("ok", false, "error", "Contraseña actual incorrecta"));

                    if (!nueva.equals(confirm))
                        return ResponseEntity.badRequest()
                                .body(Map.of("ok", false, "error", "Las contraseñas no coinciden"));

                    usuario.setPasswordHash(nueva);
                    usuarioService.actualizar(usuario);
                    return ResponseEntity.ok(Map.of("ok", true, "message", "Contraseña actualizada"));
                })
                .orElse(ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Usuario no encontrado")));
    }

    /**
     * Extrae el nombre completo del usuario desde el perfil disponible (cliente o empleado).
     * Se aísla la lógica en un método auxiliar para dejar el flujo principal más legible.
     */
    private String extraerNombreCompleto(Usuario usuario) {
        try {
            if (usuario.getClientePerfil() != null && usuario.getClientePerfil().getNombreCompleto() != null) {
                return usuario.getClientePerfil().getNombreCompleto();
            }
            if (usuario.getEmpleadoPerfil() != null && usuario.getEmpleadoPerfil().getNombreCompleto() != null) {
                return usuario.getEmpleadoPerfil().getNombreCompleto();
            }
        } catch (Exception ignored) {
            // Evitamos que una relación perezosa no inicializada rompa la respuesta del endpoint.
        }
        return null;
    }

}
