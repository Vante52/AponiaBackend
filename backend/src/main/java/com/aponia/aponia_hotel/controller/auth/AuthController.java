package com.aponia.aponia_hotel.controller.auth;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.auth.RegistroAppService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final RegistroAppService registroAppService;

    public AuthController(UsuarioService usuarioService,
                          RegistroAppService registroAppService) {
        this.usuarioService = usuarioService;
        this.registroAppService = registroAppService;
    }

    // ====== LOGIN (igual) ======
    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "logout", required = false) String logout, Model model) {
        if (logout != null) model.addAttribute("ok", "Sesión cerrada correctamente");
        return "auth/login";
    }

    @PostMapping("/login")
public String login(@RequestParam("email") String email,
                    @RequestParam("password") String password,
                    HttpSession session,
                    Model model) {

    Optional<Usuario> opt = usuarioService.findByEmail(email);

    if (opt.isPresent() && matchesPlain(password, opt.get().getPasswordHash())) {
        Usuario u = opt.get();

        // Guarda info básica en sesión
        session.setAttribute("AUTH_USER_ID", u.getId());
        session.setAttribute("AUTH_USER_EMAIL", u.getEmail());
        session.setAttribute("AUTH_USER_ROLE", u.getRol()); // guarda el enum

        // Redirige según rol
        if (u.getRol() == Usuario.UserRole.ADMIN) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/home/dashboard";
        }
    }

    model.addAttribute("error", "Correo o contraseña incorrectos");
    return "auth/login";
}



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private boolean matchesPlain(String rawPassword, String storedPasswordHash) {
        return storedPasswordHash != null && storedPasswordHash.equals(rawPassword);
    }

    // ====== REGISTRO ======

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("nombreCompleto") String nombreCompleto,
                           @RequestParam(value = "telefono", required = false) String telefono,
                           Model model,
                           HttpSession session) {
        try {
            Usuario u = registroAppService.registrarCliente(email, password, nombreCompleto, telefono);

            // Autologin
            session.setAttribute("AUTH_USER_ID", u.getId());
            session.setAttribute("AUTH_USER_EMAIL", u.getEmail());
            session.setAttribute("AUTH_USER_ROLE", u.getRol());

            return "redirect:/home/dashboard";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "auth/register";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "No se pudo registrar: " + e.getMessage());
            return "auth/register";
        }
    }

    @PostMapping("/auth/password")
public String changePassword(@RequestParam String currentPassword,
                             @RequestParam String newPassword,
                             @RequestParam String confirmPassword,
                             HttpSession session,
                             Model model) {

    // Usuario autenticado (desde la sesión)
    String userId = (String) session.getAttribute("AUTH_USER_ID");
    if (userId == null) {
        return "redirect:/login";
    }

    Optional<Usuario> optUser = usuarioService.obtener(userId);
    if (optUser.isEmpty()) {
        model.addAttribute("error", "Usuario no encontrado");
        return "redirect:/login";
    }

    Usuario user = optUser.get();

    // Validar contraseña actual
    if (!user.getPasswordHash().equals(currentPassword)) {
        model.addAttribute("error", "La contraseña actual no es correcta");
        return "redirect:/home/user_info/" + user.getEmail();
    }

    // Validar confirmación
    if (!newPassword.equals(confirmPassword)) {
        model.addAttribute("error", "Las contraseñas nuevas no coinciden");
        return "redirect:/home/user_info/" + user.getEmail();
    }

    // Guardar nueva contraseña (texto plano por ahora)
    user.setPasswordHash(newPassword);
    usuarioService.actualizar(user);

    model.addAttribute("success", "Contraseña actualizada correctamente");
    return "redirect:/home/user_info/" + user.getEmail();
}

}
