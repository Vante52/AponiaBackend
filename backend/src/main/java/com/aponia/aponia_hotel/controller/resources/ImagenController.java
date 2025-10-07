package com.aponia.aponia_hotel.controller.resources;

import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.service.resources.ImagenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/imagenes")
public class ImagenController {

    private final ImagenService service;

    public ImagenController(ImagenService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("imagenes", service.listar());
        return "imagenes/list";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        Imagen imagen = new Imagen();
        model.addAttribute("imagen", imagen);
        return "imagenes/form";
    }

    @PostMapping
    public String crear(@ModelAttribute Imagen imagen) {
        if (imagen.getId() == null || imagen.getId().isBlank()) {
            imagen.setId(UUID.randomUUID().toString());
        }
        service.crear(imagen);
        return "redirect:/imagenes";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable String id, Model model) {
        model.addAttribute("imagen", service.obtener(id));
        return "imagenes/form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable String id, @ModelAttribute Imagen imagen) {
        imagen.setId(id);
        service.actualizar(imagen);
        return "redirect:/imagenes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable String id) {
        service.eliminar(id);
        return "redirect:/imagenes";
    }
}