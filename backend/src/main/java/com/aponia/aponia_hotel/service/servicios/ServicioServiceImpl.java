package com.aponia.aponia_hotel.service.servicios;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.aponia.aponia_hotel.entities.pagos.ResumenPago;
import org.springframework.stereotype.Service;

import com.aponia.aponia_hotel.entities.servicios.Servicio;
import com.aponia.aponia_hotel.repository.servicios.ServicioRepository;

@Service
public class ServicioServiceImpl implements ServicioService {

    private final ServicioRepository repository;

    public ServicioServiceImpl(ServicioRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Servicio> listar() {
        return repository.findAll();
    }

    @Override
    public Servicio crear(Servicio servicio) {
        return repository.save(servicio);
    }

    @Override
    public Optional<Servicio> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public Servicio actualizar(Servicio servicio) {
        return repository.save(servicio);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }

//    @Override
//    public void actualizarResumen(String reservaId, BigDecimal totalHabitaciones, BigDecimal totalServicios, BigDecimal totalPagado) {
//        ResumenPago resumen = repository.findByReservaId(reservaId)
//                .orElseGet(() -> {
//                    ResumenPago nuevo = new ResumenPago();
//                    nuevo.setReservaId(reservaId);
//                    // set referencia liviana a Reserva si quieres:
//                    // Reserva r = new Reserva(); r.setId(reservaId);
//                    // nuevo.setReserva(r);
//                    return nuevo;
//                });
//
//        resumen.setTotalHabitaciones(totalHabitaciones != null ? totalHabitaciones : BigDecimal.ZERO);
//        resumen.setTotalServicios(totalServicios != null ? totalServicios : BigDecimal.ZERO);
//        resumen.setTotalPagado(totalPagado != null ? totalPagado : BigDecimal.ZERO);
//
//        // @PrePersist/@PreUpdate recalcula totalReserva y saldoPendiente
//        repository.save(resumen);
//    }
}

