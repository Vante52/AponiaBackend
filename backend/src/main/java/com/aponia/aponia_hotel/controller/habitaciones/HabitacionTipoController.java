package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionTipoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.UUID;


@Controller
@RequestMapping("/habitaciones-tipos")
public class HabitacionTipoController {

    @Autowired
    private HabitacionTipoService service;

    @Autowired
    private HabitacionService habitacionService;

    // public HabitacionTipoController(HabitacionTipoService service) {
    //     this.service = service;
    // }


    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tiposHabitacion", service.listar());
        return "habitaciones-tipos/list"; // -> templates/servicios/list.html
    }
    



    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        HabitacionTipo tipo = new HabitacionTipo();
        tipo.setActiva(true);
        tipo.setPrecioPorNoche(BigDecimal.ZERO);
        tipo.setAforoMaximo(1);
        // Por si tu validación exige ambos rangos:
        model.addAttribute("tipoHabitacion", tipo);
        return "habitaciones-tipos/form";
    }

    @GetMapping("/cards")
    public String listarCards(Model model) {
        model.addAttribute("habitacionesTipos", service.listar());
        return "habitaciones-tipos/cards"; // si también usas vista en tarjetas
    }

    @PostMapping
    public String crear(@ModelAttribute("tipoHabitacion") HabitacionTipo tipoHabitacion,
                        RedirectAttributes ra) {
        try {
            if (tipoHabitacion.getId() == null || tipoHabitacion.getId().isBlank()) {
                tipoHabitacion.setId(UUID.randomUUID().toString());
            }
            service.crear(tipoHabitacion);
            ra.addFlashAttribute("ok", "Tipo de habitación creado");
            return "redirect:/habitaciones-tipos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo crear: " + e.getMessage());
            return "redirect:/habitaciones-tipos/nuevo";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model, RedirectAttributes ra) {
        return service.obtener(id).map(tipo -> {
            model.addAttribute("tipoHabitacion", tipo);
            return "habitaciones-tipos/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "No existe el tipo solicitado");
            return "redirect:/habitaciones-tipos";
        });
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable String id,
                             @ModelAttribute("tipoHabitacion") HabitacionTipo tipoHabitacion,
                             RedirectAttributes ra) {
        try {
            tipoHabitacion.setId(id);
            service.actualizar(tipoHabitacion);
            ra.addFlashAttribute("ok", "Tipo de habitación actualizado");
            return "redirect:/habitaciones-tipos";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo actualizar: " + e.getMessage());
            return "redirect:/habitaciones-tipos/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            if(habitacionService.listarPorTipo(id).size() > 0) {
                ra.addFlashAttribute("error", "No se puede eliminar el tipo de habitación porque tiene habitaciones asociadas");
                return "redirect:/habitaciones-tipos";
            } else {
                service.eliminar(id);
                ra.addFlashAttribute("ok", "Tipo de habitación eliminado");
            }
            
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/habitaciones-tipos";
    }
    @GetMapping("/{id}")
    public String detallePublico(@PathVariable String id, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes ra) {
        return service.obtener(id)
            .map(s -> {
                model.addAttribute("habitacionTipo", s);
                return "habitaciones-tipos/detalle";
            })
            .orElseGet(() -> {
                ra.addFlashAttribute("error", "El servicio no existe");
                return "redirect:/habitaciones-tipo/cards";
            });
    }

}
