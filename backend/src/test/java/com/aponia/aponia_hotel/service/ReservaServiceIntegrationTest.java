package com.aponia.aponia_hotel.service;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.entities.usuarios.Usuario.UserRole;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;
import com.aponia.aponia_hotel.repository.usuarios.ClientePerfilRepository;
import com.aponia.aponia_hotel.service.reservas.ReservaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
@Transactional
class ReservaServiceIntegrationTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClientePerfilRepository clientePerfilRepository;

    @Autowired
    private HabitacionTipoRepository tipoRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private Usuario cliente;
    private HabitacionTipo tipo;
    private Habitacion habitacion;

    @BeforeEach
    void setUp() {
        reservaRepository.deleteAll();
        habitacionRepository.deleteAll();
        tipoRepository.deleteAll();
        clientePerfilRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = new Usuario(
                UUID.randomUUID().toString(),
                "cliente@test.com",
                "123456",
                UserRole.CLIENTE,
                null,
                null
        );
        usuarioRepository.save(cliente);

        tipo = new HabitacionTipo(
                UUID.randomUUID().toString(),
                "Suite Premium",
                "Vista al mar",
                2,
                new BigDecimal("500000"),
                true,
                null,
                null
        );
        tipoRepository.save(tipo);

        habitacion = new Habitacion(
                UUID.randomUUID().toString(),
                tipo,
                (int) (100 + Math.random() * 900),
                true
        );
        habitacionRepository.save(habitacion);
    }

    @Test
    void crearReservaCliente_creaCorrectamente() {
        Reserva reserva = crearReservaSegura("Reserva de prueba",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3));

        assertThat(reserva).isNotNull();
        assertThat(reserva.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
    }

    @Test
    void cancelarReserva_cambiaEstadoACancelada() {
        Reserva reserva = crearReservaSegura("Cancelar test",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2));

        reservaService.cancelarReserva(reserva.getId());
        Reserva cancelada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertThat(cancelada.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void completarReserva_cambiaEstadoACompletada() {
        Reserva reserva = crearReservaSegura("Completar test",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2));

        reservaService.completarReserva(reserva.getId());
        Reserva completada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertThat(completada.getEstado()).isEqualTo(EstadoReserva.COMPLETADA);
    }

    @Test
    void listarPorCliente_devuelveReservasDelCliente() {
        Reserva reserva = crearReservaSegura("Listar test",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2));

        List<Reserva> reservas = reservaService.listarPorCliente(cliente.getId());
        assertThat(reservas).isNotEmpty();
        assertThat(reservas.get(0).getCliente().getId()).isEqualTo(cliente.getId());
    }

    @Test
    void verificarDisponibilidad_devuelveTrueCuandoHayHabitaciones() {
        boolean disponible = reservaService.verificarDisponibilidad(
                tipo.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                2
        );
        assertThat(disponible).isTrue();
    }

    @Test
    void crearReservaCliente_fallaSiNoHayDisponibilidad() {
        tipo.setActiva(false);
        tipoRepository.save(tipo);

        assertThrows(IllegalStateException.class, () -> {
            reservaService.crearReservaCliente(
                    cliente.getId(),
                    tipo.getId(),
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3),
                    2,
                    "Sin disponibilidad"
            );
        });
    }

   private Reserva crearReservaSegura(String concepto, LocalDate inicio, LocalDate fin) {
    Reserva reserva = new Reserva();
    reserva.setId(UUID.randomUUID().toString());
    reserva.setCliente(cliente);
    reserva.setEstado(EstadoReserva.CONFIRMADA);
    reserva.setFechaCreacion(LocalDateTime.now());
    reserva.setCodigo("TEST-" + System.currentTimeMillis());
    reservaRepository.saveAndFlush(reserva);


    return reserva;
}
}
