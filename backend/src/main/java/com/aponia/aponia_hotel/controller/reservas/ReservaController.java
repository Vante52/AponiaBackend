package com.aponia.aponia_hotel.controller.reservas;

import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.service.reservas.ReservaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reservas", service.listar());
        return "reservas/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        Reserva reserva = new Reserva();
        reserva.setFechaCreacion(LocalDateTime.now());
        reserva.setEstado(Reserva.EstadoReserva.valueOf("pendiente"));
        model.addAttribute("reserva", reserva);
        return "reservas/form";
    }

    @PostMapping
    public String crear(@ModelAttribute Reserva reserva) {
        if (reserva.getId() == null || reserva.getId().isBlank()) {
            reserva.setId(UUID.randomUUID().toString());
        }
        // Generar código único para la reserva
        if (reserva.getCodigo() == null || reserva.getCodigo().isBlank()) {
            reserva.setCodigo("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        service.crear(reserva);
        return "redirect:/reservas";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        model.addAttribute("reserva", service.obtener(id));
        return "reservas/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable String id, @ModelAttribute Reserva reserva) {
        reserva.setId(id);
        service.actualizar(reserva);
        return "redirect:/reservas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id) {
        service.eliminar(id);
        return "redirect:/reservas";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable String id) {
        service.confirmarReserva(id);
        return "redirect:/reservas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable String id) {
        service.cancelarReserva(id);
        return "redirect:/reservas";
    }
}