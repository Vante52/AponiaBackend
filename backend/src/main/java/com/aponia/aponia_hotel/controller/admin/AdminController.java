package com.aponia.aponia_hotel.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
public String dashboard(HttpSession session) {
    Usuario.UserRole rol = (Usuario.UserRole) session.getAttribute("AUTH_USER_ROLE");
    if (rol != Usuario.UserRole.ADMIN) {
        return "redirect:/login";
    }
    return "admin/dashboard";
}

}
