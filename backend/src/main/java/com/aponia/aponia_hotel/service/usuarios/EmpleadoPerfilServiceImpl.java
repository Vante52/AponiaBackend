package com.aponia.aponia_hotel.service.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.EmpleadoPerfil;
import com.aponia.aponia_hotel.repository.usuarios.EmpleadoPerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmpleadoPerfilServiceImpl implements EmpleadoPerfilService {

    private final EmpleadoPerfilRepository repository;

    public EmpleadoPerfilServiceImpl(EmpleadoPerfilRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoPerfil> listar() {
        return repository.findAll();
    }

    @Override
    public EmpleadoPerfil crear(EmpleadoPerfil empleadoPerfil) {
        return repository.save(empleadoPerfil);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmpleadoPerfil> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public EmpleadoPerfil actualizar(EmpleadoPerfil empleadoPerfil) {
        return repository.save(empleadoPerfil);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}