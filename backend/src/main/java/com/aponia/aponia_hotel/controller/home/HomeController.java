package com.aponia.aponia_hotel.controller.home;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.service.usuarios.ClientePerfilService;
import com.aponia.aponia_hotel.service.usuarios.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/home")
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClientePerfilService clientePerfilService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("AUTH_USER_ID") == null) {
            return "redirect:/login";
        }

        Optional<Usuario> opt = usuarioService.findByEmail(session.getAttribute("AUTH_USER_EMAIL").toString());
        if (opt.isEmpty()) {
            return "redirect:/usuarios";
        }
        var u = opt.get();
        var uInfo = clientePerfilService.obtener(u.getId()).get();
        log.info(uInfo.getNombreCompleto());
        // System.out.println(uInfo.getNombreCompleto());
        model.addAttribute("usuario", u);
        model.addAttribute("clientePerfil", uInfo);

        // Puedes cargar aquí datos para cards rápidas, reservas, etc.
        return "app/dashboard";
    }

    // @GetMapping("/perfil")
    // public String perfil(HttpSession session, Model model) {
    // if (session.getAttribute("AUTH_USER_ID") == null) {
    // return "redirect:/login";
    // }
    // // Puedes cargar aquí datos para cards rápidas, reservas, etc.
    // return "app/perfil";
    // }
    @GetMapping("/user_info/{email}")
    public String userInfo(Model model, @PathVariable String email) {
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/usuarios";
        }
        var u = opt.get();
        var uInfo = clientePerfilService.obtener(u.getId()).get();
        log.info(uInfo.getNombreCompleto());
        // System.out.println(uInfo.getNombreCompleto());
        model.addAttribute("usuario", u);
        model.addAttribute("clientePerfil", uInfo);
        return "app/user_info";
    }

    @PostMapping("/user_info/{email}")
    public String updateUserInfo(
            @PathVariable String email,
            @ModelAttribute("clientePerfil") ClientePerfil clientePerfilForm,
            Model model) {

        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/usuarios";
        }

        var usuario = opt.get();
        var clientePerfilExistenteOpt = clientePerfilService.obtener(usuario.getId());
        if (clientePerfilExistenteOpt.isEmpty()) {
            return "redirect:/usuarios";
        }

        var clientePerfilExistente = clientePerfilExistenteOpt.get();

        // Copiamos los datos del formulario al objeto existente
        clientePerfilExistente.setNombreCompleto(clientePerfilForm.getNombreCompleto());
        clientePerfilExistente.setTelefono(clientePerfilForm.getTelefono());
        // 👆 aquí setea todos los campos que vengan del form

        // Guardamos con el service
        clientePerfilService.actualizar(clientePerfilExistente);

        return "redirect:/home/user_info/" + email;
    }

    @PostMapping("/user_info/{email}/delete")
    public String deleteUserInfo(@PathVariable String email, RedirectAttributes ra) {
        Optional<Usuario> opt = usuarioService.findByEmail(email);
        if (opt.isEmpty()) {
            return "redirect:/usuarios";
        }
        var u = opt.get();
        String id = u.getId();

        try {
            usuarioService.eliminar(id);
            clientePerfilService.eliminar(id);
            ra.addFlashAttribute("ok", "Usuario eliminado");
            return "redirect:/";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
            return "redirect:/";
        }
    }
}
