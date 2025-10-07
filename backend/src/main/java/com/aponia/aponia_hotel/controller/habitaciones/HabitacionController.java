package com.aponia.aponia_hotel.controller.habitaciones;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionTipoService;

@Controller
@RequestMapping("/habitaciones")
public class HabitacionController {

    private final HabitacionService habitacionService;
    private final HabitacionTipoService habitacionTipoService;

    public HabitacionController(HabitacionService habitacionService,
                                HabitacionTipoService habitacionTipoService) {
        this.habitacionService = habitacionService;
        this.habitacionTipoService = habitacionTipoService;
    }

    @GetMapping
    public String listar(Model model,
                         @ModelAttribute("ok") String ok,
                         @ModelAttribute("error") String error) {
        model.addAttribute("habitaciones", habitacionService.listar());
        return "habitaciones/list";
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        Habitacion h = new Habitacion();
        h.setActiva(true);
        model.addAttribute("habitacion", h);
        model.addAttribute("tipos", habitacionTipoService.listarActivos());
        return "habitaciones/form";
    }

    @PostMapping
    public String crear(@ModelAttribute Habitacion habitacion,
                        @RequestParam("tipoId") String tipoId,
                        RedirectAttributes ra) {
        try {
            if (habitacion.getId() == null || habitacion.getId().isBlank()) {
                habitacion.setId(UUID.randomUUID().toString());
            }
            // Asociar el tipo (solo con id para evitar fetch innecesario)
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoId);
            habitacion.setTipo(ref);

            Habitacion saved = habitacionService.crear(habitacion);
            ra.addFlashAttribute("ok", "Habitación creada (#" + saved.getNumero() + ")");
            return "redirect:/habitaciones";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo crear: " + e.getMessage());
            return "redirect:/habitaciones/nueva";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model, RedirectAttributes ra) {
        Optional<Habitacion> opt = habitacionService.obtener(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "No existe la habitación");
            return "redirect:/habitaciones";
        }
        model.addAttribute("habitacion", opt.get());
        model.addAttribute("tipos", habitacionTipoService.listarActivos());
        return "habitaciones/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable String id,
                             @ModelAttribute Habitacion habitacion,
                             @RequestParam("tipoId") String tipoId,
                             RedirectAttributes ra) {
        try {
            habitacion.setId(id);
            HabitacionTipo ref = new HabitacionTipo();
            ref.setId(tipoId);
            habitacion.setTipo(ref);

            Habitacion saved = habitacionService.actualizar(habitacion);
            ra.addFlashAttribute("ok", "Habitación actualizada (#" + saved.getNumero() + ")");
            return "redirect:/habitaciones";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo actualizar: " + e.getMessage());
            return "redirect:/habitaciones/" + id + "/editar";
        }
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id, RedirectAttributes ra) {
        try {
            habitacionService.eliminar(id);
            ra.addFlashAttribute("ok", "Habitación eliminada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "No se pudo eliminar: " + e.getMessage());
        }
        return "redirect:/habitaciones";
    }

    
}
