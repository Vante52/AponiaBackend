package com.aponia.aponia_hotel.service.servicios;

import com.aponia.aponia_hotel.entities.servicios.ServicioDisponibilidad;
import com.aponia.aponia_hotel.repository.servicios.ServicioDisponibilidadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServicioDisponibilidadServiceImpl implements ServicioDisponibilidadService {

    private final ServicioDisponibilidadRepository repository;

    public ServicioDisponibilidadServiceImpl(ServicioDisponibilidadRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDisponibilidad> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDisponibilidad> listarDisponibles(String servicioId, LocalDate fecha, int capacidadRequerida) {
        return repository.findByServicioIdAndFechaAndCapacidadDisponibleGreaterThan(
            servicioId, fecha, capacidadRequerida - 1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDisponibilidad> listarPorRangoFechas(String servicioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return repository.findDisponibilidadesByServicioAndRangoFechas(servicioId, fechaInicio, fechaFin);
    }

    @Override
    public ServicioDisponibilidad crear(ServicioDisponibilidad disponibilidad) {
        validarDisponibilidad(disponibilidad);
        return repository.save(disponibilidad);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServicioDisponibilidad> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServicioDisponibilidad> buscarDisponibilidad(
            String servicioId, LocalDate fecha, LocalTime horaInicio) {
        return Optional.ofNullable(repository.findByServicioIdAndFechaAndHoraInicio(servicioId, fecha, horaInicio));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDisponibilidad(String servicioId, LocalDate fecha, LocalTime horaInicio) {
        return repository.existsByServicioIdAndFechaAndHoraInicio(servicioId, fecha, horaInicio);
    }

    @Override
    public ServicioDisponibilidad actualizar(ServicioDisponibilidad disponibilidad) {
        validarDisponibilidad(disponibilidad);
        return repository.save(disponibilidad);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }

    private void validarDisponibilidad(ServicioDisponibilidad disponibilidad) {
        if (disponibilidad.getHoraInicio() != null &&
            disponibilidad.getHoraFin() != null &&
            !disponibilidad.getHoraFin().isAfter(disponibilidad.getHoraInicio())) {
            throw new IllegalArgumentException("La hora de fin debe ser posterior a la hora de inicio");
        }

        if (disponibilidad.getCapacidadDisponible() < 0) {
            throw new IllegalArgumentException("La capacidad disponible no puede ser negativa");
        }
    }
}