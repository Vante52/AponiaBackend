package com.aponia.aponia_hotel.repository;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.entities.reservas.Estancia;
import com.aponia.aponia_hotel.entities.reservas.Reserva;
import com.aponia.aponia_hotel.entities.reservas.Reserva.EstadoReserva;
import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.entities.usuarios.Usuario.UserRole;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import com.aponia.aponia_hotel.repository.reservas.ReservaRepository;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ReservaRepositoryTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HabitacionTipoRepository habitacionTipoRepository;

    @Autowired
    private HabitacionRepository habitacionRepository;

    private Usuario cliente;
    private HabitacionTipo tipo;
    private Habitacion habitacion;
    private Reserva reservaBase;
    private Estancia estanciaBase;

    @BeforeEach
    void setUp() {
        // Crear y guardar cliente
        cliente = new Usuario(
                UUID.randomUUID().toString(),
                "cliente@test.com",
                "123456",
                UserRole.CLIENTE,
                null,
                null);
        usuarioRepository.save(cliente);

        // Crear tipo y habitación
        tipo = new HabitacionTipo(UUID.randomUUID().toString(), "Suite Ejecutiva",
                "Habitación amplia con escritorio", 2,
                new BigDecimal("300000"), true, null, null);
        habitacionTipoRepository.save(tipo);

        habitacion = new Habitacion(UUID.randomUUID().toString(), tipo, 101, true);
        habitacionRepository.save(habitacion);

        // Crear reserva base y guardar
        reservaBase = new Reserva();
        reservaBase.setId(UUID.randomUUID().toString());
        reservaBase.setCodigo("RES001");
        reservaBase.setCliente(cliente);
        reservaBase.setFechaCreacion(LocalDateTime.now().minusDays(2));
        reservaBase.setEstado(EstadoReserva.CONFIRMADA);
        reservaBase.setNotas("Reserva base de prueba");
        reservaRepository.save(reservaBase);

        // Crear estancia asociada
        estanciaBase = new Estancia();
        estanciaBase.setId(UUID.randomUUID().toString());
        estanciaBase.setCheckIn(false);
        estanciaBase.setCheckOut(false);
        estanciaBase.setEntrada(LocalDate.now().minusDays(1));
        estanciaBase.setSalida(LocalDate.now().plusDays(1));
        estanciaBase.setNumeroHuespedes(2);
        estanciaBase.setPrecioPorNoche(new BigDecimal("300000"));
        estanciaBase.setTotalEstadia(new BigDecimal("600000"));
        estanciaBase.setReserva(reservaBase);
        estanciaBase.setTipoHabitacion(tipo);
        estanciaBase.setHabitacionAsignada(habitacion);
    }

    @Test
    void findByClienteId_devuelveReservasDelCliente() {
        List<Reserva> resultado = reservaRepository.findByClienteId(cliente.getId());
        assertThat(resultado).isNotNull();
        assertThat(resultado).extracting(Reserva::getCodigo).contains("RES001");
    }

    @Test
    void findReservasVencidas_devuelveReservasAntesDeFecha() {
        // Arrange
        LocalDateTime fechaVieja = LocalDateTime.of(2023, 1, 1, 12, 0);
        LocalDateTime fechaReciente = LocalDateTime.of(2025, 1, 1, 12, 0);
        LocalDateTime fechaCorte = LocalDateTime.of(2024, 1, 1, 12, 0);

        Reserva vieja = new Reserva();
        vieja.setId(UUID.randomUUID().toString());
        vieja.setCodigo("RES002");
        vieja.setCliente(cliente);
        vieja.setEstado(EstadoReserva.CONFIRMADA);
        vieja.setFechaCreacion(fechaVieja);
        reservaRepository.save(vieja);

        Reserva reciente = new Reserva();
        reciente.setId(UUID.randomUUID().toString());
        reciente.setCodigo("RES003");
        reciente.setCliente(cliente);
        reciente.setEstado(EstadoReserva.CONFIRMADA);
        reciente.setFechaCreacion(fechaReciente);
        reservaRepository.save(reciente);

        // Act
        List<Reserva> vencidas = reservaRepository.findReservasVencidas(
                EstadoReserva.CONFIRMADA,
                fechaCorte);

        // Assert
        assertThat(vencidas)
                .isNotEmpty()
                .extracting(Reserva::getCodigo)
                .contains("RES002")
                .doesNotContain("RES003");
    }

    @Test
    void findReservasDelDia_devuelveReservasPorEntradaYEstado() {
        Estancia estancia = new Estancia();
        estancia.setId(UUID.randomUUID().toString());
        estancia.setCheckIn(true);
        estancia.setCheckOut(false);
        estancia.setEntrada(LocalDate.now());
        estancia.setSalida(LocalDate.now().plusDays(2));
        estancia.setNumeroHuespedes(2);
        estancia.setPrecioPorNoche(new BigDecimal("200000"));
        estancia.setTotalEstadia(new BigDecimal("400000"));
        estancia.setReserva(reservaBase);
        estancia.setTipoHabitacion(tipo);
        estancia.setHabitacionAsignada(habitacion);
        reservaBase.setEstancias(List.of(estancia));
        reservaRepository.save(reservaBase);

        List<Reserva> resultado = reservaRepository.findReservasDelDia(
                EstadoReserva.CONFIRMADA,
                LocalDate.now());

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.get(0).getCodigo()).isEqualTo("RES001");
    }

    @Test
    void findEstanciasActivasPorHabitacion_devuelveEstanciasActuales() {
        estanciaBase.setCheckIn(true);
        estanciaBase.setCheckOut(false);
        reservaBase.setEstancias(List.of(estanciaBase));
        reservaRepository.save(reservaBase);

        List<Estancia> activas = reservaRepository.findEstanciasActivasPorHabitacion(habitacion.getId());
        assertThat(activas).isNotEmpty();
        assertThat(activas.get(0).getHabitacionAsignada().getNumero()).isEqualTo(101);
    }

    @Test
    void findReservasRecientes_devuelveReservasDelUltimoMes() {
        List<Reserva> recientes = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaCreacion().isAfter(LocalDateTime.now().minusDays(30)))
                .toList();
        assertThat(recientes).extracting(Reserva::getCodigo).contains("RES001");
    }
}
