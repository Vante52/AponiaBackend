package com.aponia.aponia_hotel.service.habitaciones;

import com.aponia.aponia_hotel.entities.habitaciones.HabitacionTipo;
import com.aponia.aponia_hotel.repository.habitaciones.HabitacionTipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class HabitacionTipoServiceImpl implements HabitacionTipoService {

    private final HabitacionTipoRepository repository;

    public HabitacionTipoServiceImpl(HabitacionTipoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionTipo> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HabitacionTipo> listarActivos() {
        return repository.findByActivaIsTrue();
    }

    @Override
    public HabitacionTipo crear(HabitacionTipo tipo) {
        if (repository.existsByNombre(tipo.getNombre())) {
            throw new IllegalArgumentException("Ya existe un tipo de habitación con ese nombre");
        }
        return repository.save(tipo);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HabitacionTipo> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public HabitacionTipo actualizar(HabitacionTipo tipo) {
        Optional<HabitacionTipo> existente = repository.findById(tipo.getId());
        if (existente.isPresent() && !existente.get().getNombre().equals(tipo.getNombre())) {
            if (repository.existsByNombre(tipo.getNombre())) {
                throw new IllegalArgumentException("Ya existe un tipo de habitación con ese nombre");
            }
        }
        return repository.save(tipo);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}