package com.aponia.aponia_hotel.controller.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.controller.habitaciones.HabitacionTipoRestController;
import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionService;
import com.aponia.aponia_hotel.service.habitaciones.HabitacionTipoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HabitacionTipoRestController.class)
class HabitacionTipoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HabitacionTipoService habitacionTipoService;

    @MockBean
    private HabitacionService habitacionService;

    @Autowired
    private ObjectMapper objectMapper;

    // ======= 1️⃣ GET: listar todos =======
    @Test
    void listar_deberiaRetornarListaDeTipos() throws Exception {
        HabitacionTipo tipo = new HabitacionTipo(
                UUID.randomUUID().toString(), "Suite Premium", "Vista al mar",
                2, new BigDecimal("500000"), true, List.of(), List.of());

        when(habitacionTipoService.listar()).thenReturn(List.of(tipo));

        mockMvc.perform(get("/api/habitaciones-tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Suite Premium"));
    }

    // ======= 2️⃣ GET: listar activos =======
    @Test
    void listarActivos_deberiaRetornarSoloActivos() throws Exception {
        HabitacionTipo tipo = new HabitacionTipo(
                UUID.randomUUID().toString(), "Estandar", "Cómoda y económica",
                2, new BigDecimal("200000"), true, List.of(), List.of());

        when(habitacionTipoService.listarActivos()).thenReturn(List.of(tipo));

        mockMvc.perform(get("/api/habitaciones-tipos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].activa").value(true));
    }

    // ======= 3️⃣ GET: buscar por ID =======
    @Test
    void findById_deberiaRetornarTipoSiExiste() throws Exception {
        HabitacionTipo tipo = new HabitacionTipo(
                "tipo123", "Deluxe", "Amplia y moderna",
                3, new BigDecimal("350000"), true, List.of(), List.of());

        when(habitacionTipoService.obtener("tipo123")).thenReturn(Optional.of(tipo));

        mockMvc.perform(get("/api/habitaciones-tipos/tipo123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Deluxe"));
    }

    @Test
    void findById_deberiaRetornar404SiNoExiste() throws Exception {
        when(habitacionTipoService.obtener("noExiste")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/habitaciones-tipos/noExiste"))
                .andExpect(status().isNotFound());
    }

    // ======= 4️⃣ POST: crear nuevo tipo =======
    @Test
    void create_deberiaCrearYRetornarTipo() throws Exception {
        HabitacionTipo tipo = new HabitacionTipo(
                null, "Familiar", "Amplia para familias",
                4, new BigDecimal("600000"), true, List.of(), List.of());

        HabitacionTipo creado = new HabitacionTipo(
                UUID.randomUUID().toString(), "Familiar", "Amplia para familias",
                4, new BigDecimal("600000"), true, List.of(), List.of());

        when(habitacionTipoService.crear(any(HabitacionTipo.class))).thenReturn(creado);

        mockMvc.perform(post("/api/habitaciones-tipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Familiar"));
    }

    // ======= 5️⃣ PUT: actualizar existente =======
    @Test
    void update_deberiaActualizarYRetornar200() throws Exception {
        HabitacionTipo tipo = new HabitacionTipo(
                "tipo1", "Estandar Renovada", "Actualizada",
                2, new BigDecimal("250000"), true, List.of(), List.of());

        doReturn(tipo).when(habitacionTipoService).actualizar(any(HabitacionTipo.class));

        mockMvc.perform(put("/api/habitaciones-tipos/tipo1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tipo)))
                .andExpect(status().isOk());
    }

    // ======= 6️⃣ DELETE: eliminar =======
    @Test
    void delete_deberiaEliminarSiNoHayHabitacionesAsociadas() throws Exception {
        when(habitacionService.listarPorTipo("tipo1")).thenReturn(List.of());
        doNothing().when(habitacionTipoService).eliminar("tipo1");

        mockMvc.perform(delete("/api/habitaciones-tipos/tipo1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_deberiaRetornar409SiTieneHabitacionesAsociadas() throws Exception {
        Habitacion habitacion = new Habitacion("h1", null, 101, true);
        when(habitacionService.listarPorTipo("tipo1")).thenReturn(List.of(habitacion));

        mockMvc.perform(delete("/api/habitaciones-tipos/tipo1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("No se puede eliminar: hay habitaciones asociadas"));
    }
}
