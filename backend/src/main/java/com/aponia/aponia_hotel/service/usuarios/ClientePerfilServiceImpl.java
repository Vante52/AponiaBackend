package com.aponia.aponia_hotel.service.usuarios;

import com.aponia.aponia_hotel.entities.usuarios.ClientePerfil;
import com.aponia.aponia_hotel.repository.usuarios.ClientePerfilRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientePerfilServiceImpl implements ClientePerfilService {

    private final ClientePerfilRepository repository;

    public ClientePerfilServiceImpl(ClientePerfilRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientePerfil> listar() {
        return repository.findAll();
    }

    @Override
    public ClientePerfil crear(ClientePerfil clientePerfil) {
        return repository.save(clientePerfil);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClientePerfil> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public ClientePerfil actualizar(ClientePerfil clientePerfil) {
        return repository.save(clientePerfil);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}