package com.aponia.aponia_hotel.service.resources;

import com.aponia.aponia_hotel.entities.resources.Imagen;
import com.aponia.aponia_hotel.repository.resources.ImagenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ImagenServiceImpl implements ImagenService {

    private final ImagenRepository repository;

    public ImagenServiceImpl(ImagenRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> listar() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> listarPorServicio(String servicioId) {
        return repository.findByServicioId(servicioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Imagen> listarPorTipoHabitacion(String tipoHabitacionId) {
        return repository.findByTipoHabitacionId(tipoHabitacionId);
    }

    @Override
    public Imagen crear(Imagen imagen) {
        return repository.save(imagen);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Imagen> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public Imagen actualizar(Imagen imagen) {
        return repository.save(imagen);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}