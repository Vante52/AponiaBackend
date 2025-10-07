package com.aponia.aponia_hotel.service.usuarios;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aponia.aponia_hotel.entities.usuarios.Usuario;
import com.aponia.aponia_hotel.repository.usuarios.UsuarioRepository;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioServiceImpl(UsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Override
    public Usuario crear(Usuario usuario) {
        return repository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> obtener(String id) {
        return repository.findById(id);
    }

    @Override
    public Usuario actualizar(Usuario usuario) {
        return repository.save(usuario);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}