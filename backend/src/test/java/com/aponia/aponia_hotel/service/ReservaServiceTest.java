package com.aponia.aponia_hotel.service;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;
import com.aponia.aponia_hotel.service.reservas.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private HabitacionTipoRepository tipoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private HabitacionRepository habitacionRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Usuario cliente;
    private HabitacionTipo tipo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cliente = new Usuario();
        cliente.setId("user123");
        cliente.setEmail("mock@test.com");

        tipo = new HabitacionTipo();
        tipo.setId("tipo123");
        tipo.setActiva(true);
        tipo.setAforoMaximo(2);
        tipo.setPrecioPorNoche(new java.math.BigDecimal("200000")); // ✅ agregado
    }

    @Test
    void crearReservaCliente_devuelveReservaConfirmada() {
        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(cliente));
        when(tipoRepository.findById("tipo123")).thenReturn(Optional.of(tipo));

        when(habitacionRepository.findHabitacionesDisponibles(any(), any(), any()))
                .thenReturn(List.of(new Habitacion()));

        Reserva mockReserva = new Reserva();
        mockReserva.setId("res123");
        mockReserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.save(any(Reserva.class))).thenReturn(mockReserva);
        when(reservaRepository.saveAndFlush(any(Reserva.class))).thenReturn(mockReserva); // ✅ agregado

        Reserva resultado = reservaService.crearReservaCliente(
                "user123",
                "tipo123",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                2,
                "Reserva mock");

        assertThat(resultado).isNotNull();
        assertThat(resultado.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);

        // ✅ Ajuste: el servicio usa saveAndFlush, no save
        verify(reservaRepository, atLeastOnce()).saveAndFlush(any(Reserva.class));
        verify(reservaRepository, atLeastOnce()).existsByCodigo(anyString());

    }

    @Test
    void cancelarReserva_cambiaEstadoACancelada() {
        Reserva reserva = new Reserva();
        reserva.setId("res456");
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById("res456")).thenReturn(Optional.of(reserva));

        reservaService.cancelarReserva("res456");

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    void completarReserva_cambiaEstadoACompletada() {
        Reserva reserva = new Reserva();
        reserva.setId("res789");
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById("res789")).thenReturn(Optional.of(reserva));

        reservaService.completarReserva("res789");

        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.COMPLETADA);
        verify(reservaRepository, times(1)).save(reserva);
    }

    @Test
    void crearReservaCliente_fallaSiTipoInactivo() {
        tipo.setActiva(false);
        when(usuarioRepository.findById("user123")).thenReturn(Optional.of(cliente));
        when(tipoRepository.findById("tipo123")).thenReturn(Optional.of(tipo));

        IllegalStateException ex = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> reservaService.crearReservaCliente(
                        "user123", "tipo123",
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(3),
                        2,
                        "Prueba tipo inactivo"));

        // ✅ mensaje corregido
        assertThat(ex.getMessage().toLowerCase()).contains("no está disponible");
    }
}
