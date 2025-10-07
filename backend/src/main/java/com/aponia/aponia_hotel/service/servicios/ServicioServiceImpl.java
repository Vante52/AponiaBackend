package com.aponia.aponia_hotel.service.servicios;

import java.util.List;
import java.util.Optional;

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
}
