package com.aponia.aponia_hotel.service.habitaciones;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.habitaciones.Habitacion;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionRepository;

@Service
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository repository;

    public HabitacionServiceImpl(HabitacionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habitacion> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habitacion> listarActivas() {
        return repository.findByActivaIsTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habitacion> listarPorTipo(String tipoId) {
        return repository.findByTipoIdAndActivaIsTrue(tipoId);
    }

    @Override
    public Habitacion crear(Habitacion habitacion) {
        if (repository.existsByNumero(habitacion.getNumero())) {
            throw new IllegalArgumentException("Ya existe una habitación con ese número");
        }
        return repository.save(habitacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Habitacion> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Habitacion> obtenerPorNumeroHabitacion(Integer numero) {
        return repository.findByNumero(numero);
    }

    @Override
    public Habitacion actualizar(Habitacion habitacion) {
        Optional<Habitacion> existente = repository.findById(habitacion.getId());
        if (existente.isPresent() && !existente.get().getNumero().equals(habitacion.getNumero())) {
            if (repository.existsByNumero(habitacion.getNumero())) {
                throw new IllegalArgumentException("Ya existe una habitación con ese número");
            }
        }
        return repository.save(habitacion);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
